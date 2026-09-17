package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.Region;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.RegionRequest;
import com.bankjatim.jims.dto.RegionResponse;
import com.bankjatim.jims.repository.OrganizationRepository;
import com.bankjatim.jims.repository.RegionRepository;
import com.bankjatim.jims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RegionResponse> getRegions() {
        List<Region> regions = regionRepository.findAllByOrderByCodeAsc();
        List<Organization> allOrgsWithRegion = organizationRepository.findAllWithParentAndRegion();

        Map<Long, List<Organization>> orgsByRegion = new HashMap<>();
        for (Organization org : allOrgsWithRegion) {
            if (org.getRegion() != null && org.getRegion().getId() != null) {
                orgsByRegion.computeIfAbsent(org.getRegion().getId(), k -> new ArrayList<>()).add(org);
            }
        }

        return regions.stream()
                .map(region -> RegionResponse.from(region, orgsByRegion.get(region.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionResponse getRegionById(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wilayah tidak ditemukan dengan ID: " + id));
        List<Organization> orgs = organizationRepository.findByRegionId(id);
        return RegionResponse.from(region, orgs);
    }

    @Transactional
    public RegionResponse createRegion(RegionRequest request) {
        if (regionRepository.findByCode(request.getCode().trim().toUpperCase()).isPresent()) {
            throw new RuntimeException("Kode wilayah " + request.getCode() + " sudah digunakan.");
        }

        Region region = Region.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Region savedRegion = regionRepository.save(region);

        List<Organization> affiliatedOrgs = new ArrayList<>();
        if (request.getOrganizationIds() != null && !request.getOrganizationIds().isEmpty()) {
            List<Organization> orgsToAssign = organizationRepository.findAllById(request.getOrganizationIds());
            for (Organization org : orgsToAssign) {
                org.setRegion(savedRegion);
            }
            affiliatedOrgs = organizationRepository.saveAll(orgsToAssign);
        }

        return RegionResponse.from(savedRegion, affiliatedOrgs);
    }

    @Transactional
    public RegionResponse updateRegion(Long id, RegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wilayah tidak ditemukan dengan ID: " + id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!region.getCode().equalsIgnoreCase(newCode)) {
            regionRepository.findByCode(newCode).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Kode wilayah " + newCode + " sudah digunakan.");
                }
            });
            region.setCode(newCode);
        }

        region.setName(request.getName().trim());
        region.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            region.setIsActive(request.getIsActive());
        }

        Region savedRegion = regionRepository.save(region);

        if (request.getOrganizationIds() != null) {
            List<Organization> currentOrgs = organizationRepository.findByRegionId(id);
            Set<Long> targetOrgIds = new HashSet<>(request.getOrganizationIds());

            for (Organization current : currentOrgs) {
                if (!targetOrgIds.contains(current.getId())) {
                    current.setRegion(null);
                    organizationRepository.save(current);
                }
            }

            List<Organization> newOrgs = organizationRepository.findAllById(targetOrgIds);
            for (Organization org : newOrgs) {
                org.setRegion(savedRegion);
            }
            organizationRepository.saveAll(newOrgs);
        }

        List<Organization> updatedAffiliatedOrgs = organizationRepository.findByRegionId(id);
        return RegionResponse.from(savedRegion, updatedAffiliatedOrgs);
    }

    @Transactional
    public void deleteRegion(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wilayah tidak ditemukan dengan ID: " + id));

        List<Organization> orgs = organizationRepository.findByRegionId(id);
        for (Organization org : orgs) {
            org.setRegion(null);
        }
        organizationRepository.saveAll(orgs);

        List<User> users = userRepository.findByRegionId(id);
        for (User user : users) {
            user.setRegion(null);
        }
        userRepository.saveAll(users);

        regionRepository.delete(region);
    }

    @Transactional
    public RegionResponse assignBranches(Long id, List<Long> organizationIds) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wilayah tidak ditemukan dengan ID: " + id));

        List<Organization> currentOrgs = organizationRepository.findByRegionId(id);
        Set<Long> targetOrgIds = new HashSet<>(organizationIds != null ? organizationIds : Collections.emptyList());

        for (Organization current : currentOrgs) {
            if (!targetOrgIds.contains(current.getId())) {
                current.setRegion(null);
                organizationRepository.save(current);
            }
        }

        if (!targetOrgIds.isEmpty()) {
            List<Organization> newOrgs = organizationRepository.findAllById(targetOrgIds);
            for (Organization org : newOrgs) {
                org.setRegion(region);
            }
            organizationRepository.saveAll(newOrgs);
        }

        List<Organization> updatedAffiliatedOrgs = organizationRepository.findByRegionId(id);
        return RegionResponse.from(region, updatedAffiliatedOrgs);
    }
}
