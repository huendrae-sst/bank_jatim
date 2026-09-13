#!/usr/bin/env python3
"""
Bank Jatim - JIMS (Jatim Inventory Management System)
Comprehensive ERD Generator for Draw.io (diagrams.net)

This script generates:
- docs/bank_jatim_erd.drawio
- erd.drawio (root workspace copy)

Includes 5 multi-page diagrams with Crow's Foot notation, color-coded functional domains,
and detailed attribute specifications for all 45 database tables.
"""

import html
import os
import xml.etree.ElementTree as ET

# =============================================================================
# DOMAIN COLOR SCHEMES & CONFIGURATIONS
# =============================================================================

DOMAINS = {
    "master": {
        "title": "1. MASTER DATA & ORGANISASI (BANK JATIM)",
        "fill": "#FFF9E6",
        "border": "#D6B656",
        "header_fill": "#FFE599",
        "header_font": "#7F6000",
        "edge_color": "#B45F06",
    },
    "procurement": {
        "title": "2. PENGADAAN, ANGGARAN & KONSOLIDASI PR-PO",
        "fill": "#EFF6FF",
        "border": "#6C8EBF",
        "header_fill": "#DAE8FC",
        "header_font": "#1E3A8A",
        "edge_color": "#2563EB",
    },
    "orders": {
        "title": "3. PERMINTAAN CABANG & SWITCHING STOCK",
        "fill": "#F0FDF4",
        "border": "#82B366",
        "header_fill": "#D5E8D4",
        "header_font": "#166534",
        "edge_color": "#16A34A",
    },
    "logistics": {
        "title": "4. FULFILLMENT GUDANG & EKSPEDISI DISTRIBUSI",
        "fill": "#FAF5FF",
        "border": "#9673A6",
        "header_fill": "#E1D5E7",
        "header_font": "#581C87",
        "edge_color": "#9333EA",
    },
    "inventory": {
        "title": "5. STOCK BALANCE ENGINE & KARTU STOK",
        "fill": "#ECFDF5",
        "border": "#27AE60",
        "header_fill": "#D1FAE5",
        "header_font": "#065F46",
        "edge_color": "#059669",
    },
    "emboss": {
        "title": "6. PERSONALISASI KARTU ATM (EMBOSS CORE BANKING)",
        "fill": "#FFF1F2",
        "border": "#E11D48",
        "header_fill": "#FFE4E6",
        "header_font": "#9F1239",
        "edge_color": "#E11D48",
    },
    "reverse": {
        "title": "7. REVERSE INVENTORY & PEMUSNAHAN BARANG",
        "fill": "#FFF7ED",
        "border": "#EA580C",
        "header_fill": "#FFEDD5",
        "header_font": "#9A3412",
        "edge_color": "#EA580C",
    },
    "finance": {
        "title": "8. SETTLEMENT ANTARUNIT, GL AKUNTANSI & AUDIT",
        "fill": "#F8FAFC",
        "border": "#64748B",
        "header_fill": "#E2E8F0",
        "header_font": "#1E293B",
        "edge_color": "#475569",
    }
}

TABLES = {
    # 1. Master Data
    "organizations": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("code", "VARCHAR(50)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("type", "VARCHAR(50)", "", None),
            ("parent_id", "BIGINT", "FK", "organizations.id"),
            ("address", "TEXT", "", None),
            ("city", "VARCHAR(100)", "", None),
            ("phone", "VARCHAR(50)", "", None),
            ("cost_center_code", "VARCHAR(50)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "warehouses": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("code", "VARCHAR(50)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("type", "VARCHAR(50)", "", None),
            ("address", "TEXT", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "users": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("name", "VARCHAR(255)", "", None),
            ("email", "VARCHAR(255)", "UQ", None),
            ("nip", "VARCHAR(50)", "UQ", None),
            ("password", "VARCHAR(255)", "", None),
            ("role", "VARCHAR(50)", "", None),
            ("approval_limit", "NUMERIC(15,2)", "", None),
            ("phone", "VARCHAR(50)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "categories": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("code", "VARCHAR(50)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("description", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "items": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("category_id", "BIGINT", "FK", "categories.id"),
            ("sku", "VARCHAR(100)", "UQ", None),
            ("barcode", "VARCHAR(100)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("uom", "VARCHAR(50)", "", None),
            ("specification", "TEXT", "", None),
            ("min_stock", "INTEGER", "", None),
            ("max_stock", "INTEGER", "", None),
            ("safety_stock", "INTEGER", "", None),
            ("reorder_point", "INTEGER", "", None),
            ("lead_time_days", "INTEGER", "", None),
            ("estimated_unit_price", "NUMERIC(15,2)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "item_conversions": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("from_uom", "VARCHAR(50)", "", None),
            ("to_uom", "VARCHAR(50)", "", None),
            ("multiplier", "NUMERIC(12,4)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "vendors": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("code", "VARCHAR(50)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("email", "VARCHAR(255)", "", None),
            ("phone", "VARCHAR(50)", "", None),
            ("address", "TEXT", "", None),
            ("sla_days", "INTEGER", "", None),
            ("payment_terms", "VARCHAR(100)", "", None),
            ("rating", "NUMERIC(3,2)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "couriers": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("code", "VARCHAR(50)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("service_types", "JSONB", "", None),
            ("sla_days", "INTEGER", "", None),
            ("phone", "VARCHAR(50)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "expedition_mappings": {
        "domain": "master",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("destination_organization_id", "BIGINT", "FK", "organizations.id"),
            ("courier_id", "BIGINT", "FK", "couriers.id"),
            ("service_type", "VARCHAR(50)", "", None),
            ("estimated_lead_days", "INTEGER", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 2. Budget & Procurement
    "budgets": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("cost_center_code", "VARCHAR(50)", "", None),
            ("year", "INTEGER", "", None),
            ("allocated_amount", "NUMERIC(15,2)", "", None),
            ("committed_amount", "NUMERIC(15,2)", "", None),
            ("realized_amount", "NUMERIC(15,2)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "purchase_requests": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("pr_number", "VARCHAR(100)", "UQ", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("procurement_method", "VARCHAR(50)", "", None),
            ("purpose", "TEXT", "", None),
            ("estimated_total_cost", "NUMERIC(15,2)", "", None),
            ("budget_status", "VARCHAR(50)", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("rejection_reason", "TEXT", "", None),
            ("submitted_at", "TIMESTAMPTZ", "", None),
            ("approved_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "purchase_request_items": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("purchase_request_id", "BIGINT", "FK", "purchase_requests.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_requested", "INTEGER", "", None),
            ("qty_approved", "INTEGER", "", None),
            ("qty_ordered", "INTEGER", "", None),
            ("estimated_unit_price", "NUMERIC(15,2)", "", None),
            ("estimated_subtotal", "NUMERIC(15,2)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "purchase_orders": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("po_number", "VARCHAR(100)", "UQ", None),
            ("vendor_id", "BIGINT", "FK", "vendors.id"),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("order_date", "DATE", "", None),
            ("expected_delivery_date", "DATE", "", None),
            ("subtotal", "NUMERIC(15,2)", "", None),
            ("tax_amount", "NUMERIC(15,2)", "", None),
            ("total_amount", "NUMERIC(15,2)", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("rejection_reason", "TEXT", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "purchase_order_items": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("purchase_order_id", "BIGINT", "FK", "purchase_orders.id"),
            ("purchase_request_item_id", "BIGINT", "FK", "purchase_request_items.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_ordered", "INTEGER", "", None),
            ("qty_received", "INTEGER", "", None),
            ("unit_price", "NUMERIC(15,2)", "", None),
            ("subtotal", "NUMERIC(15,2)", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "goods_receipts": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("grn_number", "VARCHAR(100)", "UQ", None),
            ("purchase_order_id", "BIGINT", "FK", "purchase_orders.id"),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("received_by_user_id", "BIGINT", "FK", "users.id"),
            ("vendor_delivery_note_number", "VARCHAR(100)", "", None),
            ("receipt_date", "DATE", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "goods_receipt_items": {
        "domain": "procurement",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("goods_receipt_id", "BIGINT", "FK", "goods_receipts.id"),
            ("purchase_order_item_id", "BIGINT", "FK", "purchase_order_items.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_received", "INTEGER", "", None),
            ("qty_accepted", "INTEGER", "", None),
            ("qty_rejected", "INTEGER", "", None),
            ("condition_notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 3. Branch Orders & Switching
    "orders": {
        "domain": "orders",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("order_number", "VARCHAR(100)", "UQ", None),
            ("requesting_organization_id", "BIGINT", "FK", "organizations.id"),
            ("requesting_warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("priority", "VARCHAR(50)", "", None),
            ("required_date", "DATE", "", None),
            ("total_items", "INTEGER", "", None),
            ("total_estimated_value", "NUMERIC(15,2)", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("is_overbudget", "BOOLEAN", "", None),
            ("delivery_method", "VARCHAR(50)", "", None),
            ("pickup_pic_nip", "VARCHAR(50)", "", None),
            ("pickup_pic_name", "VARCHAR(255)", "", None),
            ("pickup_pic_position", "VARCHAR(255)", "", None),
            ("pickup_notes", "TEXT", "", None),
            ("rejection_reason", "TEXT", "", None),
            ("notes", "TEXT", "", None),
            ("submitted_at", "TIMESTAMPTZ", "", None),
            ("approved_at", "TIMESTAMPTZ", "", None),
            ("completed_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "order_items": {
        "domain": "orders",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_requested", "INTEGER", "", None),
            ("qty_approved", "INTEGER", "", None),
            ("qty_allocated", "INTEGER", "", None),
            ("qty_picked", "INTEGER", "", None),
            ("qty_packed", "INTEGER", "", None),
            ("qty_shipped", "INTEGER", "", None),
            ("qty_received", "INTEGER", "", None),
            ("unit_price_ref", "NUMERIC(15,2)", "", None),
            ("subtotal_ref", "NUMERIC(15,2)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "order_allocations": {
        "domain": "orders",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("order_item_id", "BIGINT", "FK", "order_items.id"),
            ("source_warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("qty_allocated", "INTEGER", "", None),
            ("allocation_type", "VARCHAR(50)", "", None),
            ("switching_stock_id", "BIGINT", "FK", "switching_stocks.id"),
            ("status", "VARCHAR(50)", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "switching_stocks": {
        "domain": "orders",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("source_organization_id", "BIGINT", "FK", "organizations.id"),
            ("source_warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("destination_organization_id", "BIGINT", "FK", "organizations.id"),
            ("destination_warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("proposed_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("status", "VARCHAR(50)", "", None),
            ("recommendation_reason", "TEXT", "", None),
            ("rejection_reason", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "switching_stock_items": {
        "domain": "orders",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("switching_stock_id", "BIGINT", "FK", "switching_stocks.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_requested", "INTEGER", "", None),
            ("qty_approved", "INTEGER", "", None),
            ("qty_transferred", "INTEGER", "", None),
            ("qty_received", "INTEGER", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 4. Logistics & Fulfillment
    "warehouse_pickings": {
        "domain": "logistics",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("picking_number", "VARCHAR(100)", "UQ", None),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("picked_by_user_id", "BIGINT", "FK", "users.id"),
            ("status", "VARCHAR(50)", "", None),
            ("picked_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "warehouse_packings": {
        "domain": "logistics",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("packing_number", "VARCHAR(100)", "UQ", None),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("packed_by_user_id", "BIGINT", "FK", "users.id"),
            ("koli_count", "INTEGER", "", None),
            ("total_weight_kg", "NUMERIC(8,2)", "", None),
            ("dimensions_cm", "VARCHAR(50)", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("packed_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "shipments": {
        "domain": "logistics",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("manifest_number", "VARCHAR(100)", "UQ", None),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("switching_stock_id", "BIGINT", "FK", "switching_stocks.id"),
            ("origin_warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("destination_organization_id", "BIGINT", "FK", "organizations.id"),
            ("courier_id", "BIGINT", "FK", "couriers.id"),
            ("service_type", "VARCHAR(50)", "", None),
            ("tracking_number", "VARCHAR(100)", "", None),
            ("dispatched_by_user_id", "BIGINT", "FK", "users.id"),
            ("koli_count", "INTEGER", "", None),
            ("total_weight_kg", "NUMERIC(8,2)", "", None),
            ("shipping_cost", "NUMERIC(15,2)", "", None),
            ("eta_date", "DATE", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("dispatched_at", "TIMESTAMPTZ", "", None),
            ("delivered_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "receivings": {
        "domain": "logistics",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("receiving_number", "VARCHAR(100)", "UQ", None),
            ("shipment_id", "BIGINT", "FK", "shipments.id"),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("switching_stock_id", "BIGINT", "FK", "switching_stocks.id"),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("received_by_user_id", "BIGINT", "FK", "users.id"),
            ("receipt_date", "DATE", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("pod_signature", "TEXT", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "discrepancies": {
        "domain": "logistics",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("receiving_id", "BIGINT", "FK", "receivings.id"),
            ("order_item_id", "BIGINT", "FK", "order_items.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("discrepancy_type", "VARCHAR(50)", "", None),
            ("qty_expected", "INTEGER", "", None),
            ("qty_actual", "INTEGER", "", None),
            ("qty_damaged", "INTEGER", "", None),
            ("resolution_status", "VARCHAR(50)", "", None),
            ("berita_acara_number", "VARCHAR(100)", "", None),
            ("berita_acara_url", "TEXT", "", None),
            ("resolution_notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 5. Inventory Engine
    "stock_balances": {
        "domain": "inventory",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("on_hand", "INTEGER", "", None),
            ("reserved", "INTEGER", "", None),
            ("allocated", "INTEGER", "", None),
            ("in_transit", "INTEGER", "", None),
            ("hold", "INTEGER", "", None),
            ("damaged", "INTEGER", "", None),
            ("min_stock_override", "INTEGER", "", None),
            ("max_stock_override", "INTEGER", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "stock_ledgers": {
        "domain": "inventory",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("transaction_type", "VARCHAR(100)", "", None),
            ("reference_number", "VARCHAR(100)", "", None),
            ("qty_in", "INTEGER", "", None),
            ("qty_out", "INTEGER", "", None),
            ("balance_after", "INTEGER", "", None),
            ("unit_cost", "NUMERIC(15,2)", "", None),
            ("total_value", "NUMERIC(15,2)", "", None),
            ("notes", "TEXT", "", None),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("created_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "stock_adjustments": {
        "domain": "inventory",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("adjustment_number", "VARCHAR(100)", "UQ", None),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("adjustment_type", "VARCHAR(50)", "", None),
            ("qty_change", "INTEGER", "", None),
            ("reason", "TEXT", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("requested_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "stock_opnames": {
        "domain": "inventory",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("opname_number", "VARCHAR(100)", "UQ", None),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("conducted_by_user_id", "BIGINT", "FK", "users.id"),
            ("verified_by_user_id", "BIGINT", "FK", "users.id"),
            ("opname_date", "DATE", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "stock_opname_items": {
        "domain": "inventory",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("stock_opname_id", "BIGINT", "FK", "stock_opnames.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_system", "INTEGER", "", None),
            ("qty_physical", "INTEGER", "", None),
            ("qty_difference", "INTEGER", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 6. Emboss & Personalization
    "emboss_files": {
        "domain": "emboss",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("filename", "VARCHAR(255)", "", None),
            ("file_path", "TEXT", "", None),
            ("total_records", "INTEGER", "", None),
            ("valid_records", "INTEGER", "", None),
            ("rejected_records", "INTEGER", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("error_message", "TEXT", "", None),
            ("uploaded_by_user_id", "BIGINT", "FK", "users.id"),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "emboss_records": {
        "domain": "emboss",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("emboss_file_id", "BIGINT", "FK", "emboss_files.id"),
            ("account_number", "VARCHAR(50)", "", None),
            ("customer_name", "VARCHAR(255)", "", None),
            ("card_number_masked", "VARCHAR(50)", "", None),
            ("card_type", "VARCHAR(50)", "", None),
            ("branch_code", "VARCHAR(50)", "", None),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("pin_envelope_item_id", "BIGINT", "FK", "items.id"),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("status", "VARCHAR(50)", "", None),
            ("rejection_reason", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "production_orders": {
        "domain": "emboss",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("production_number", "VARCHAR(100)", "UQ", None),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("production_date", "DATE", "", None),
            ("total_qty", "INTEGER", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "production_order_items": {
        "domain": "emboss",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("production_order_id", "BIGINT", "FK", "production_orders.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_planned", "INTEGER", "", None),
            ("qty_produced", "INTEGER", "", None),
            ("qty_damaged", "INTEGER", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 7. Reverse Logistics
    "inventory_returns": {
        "domain": "reverse",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("return_number", "VARCHAR(100)", "UQ", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("destination_warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("reason", "TEXT", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("shipped_at", "TIMESTAMPTZ", "", None),
            ("received_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "inventory_return_items": {
        "domain": "reverse",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("inventory_return_id", "BIGINT", "FK", "inventory_returns.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_returned", "INTEGER", "", None),
            ("qty_received", "INTEGER", "", None),
            ("condition", "VARCHAR(50)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "stock_destructions": {
        "domain": "reverse",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("destruction_number", "VARCHAR(100)", "UQ", None),
            ("warehouse_id", "BIGINT", "FK", "warehouses.id"),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("reason", "TEXT", "", None),
            ("berita_acara_number", "VARCHAR(100)", "", None),
            ("berita_acara_url", "TEXT", "", None),
            ("witness_1_name", "VARCHAR(255)", "", None),
            ("witness_2_name", "VARCHAR(255)", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("executed_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "stock_destruction_items": {
        "domain": "reverse",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("stock_destruction_id", "BIGINT", "FK", "stock_destructions.id"),
            ("item_id", "BIGINT", "FK", "items.id"),
            ("qty_destroyed", "INTEGER", "", None),
            ("estimated_value", "NUMERIC(15,2)", "", None),
            ("notes", "TEXT", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },

    # 8. Finance, GL & Audit
    "settlements": {
        "domain": "finance",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("settlement_number", "VARCHAR(100)", "UQ", None),
            ("order_id", "BIGINT", "FK", "orders.id"),
            ("debit_organization_id", "BIGINT", "FK", "organizations.id"),
            ("credit_organization_id", "BIGINT", "FK", "organizations.id"),
            ("debit_cost_center", "VARCHAR(50)", "", None),
            ("credit_cost_center", "VARCHAR(50)", "", None),
            ("item_amount", "NUMERIC(15,2)", "", None),
            ("shipping_amount", "NUMERIC(15,2)", "", None),
            ("total_amount", "NUMERIC(15,2)", "", None),
            ("status", "VARCHAR(50)", "", None),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("approved_by_user_id", "BIGINT", "FK", "users.id"),
            ("posted_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "chart_of_accounts": {
        "domain": "finance",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("account_code", "VARCHAR(50)", "UQ", None),
            ("account_name", "VARCHAR(255)", "", None),
            ("account_type", "VARCHAR(50)", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "cost_centers": {
        "domain": "finance",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("code", "VARCHAR(50)", "UQ", None),
            ("name", "VARCHAR(255)", "", None),
            ("description", "TEXT", "", None),
            ("is_active", "BOOLEAN", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "general_ledger_entries": {
        "domain": "finance",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("transaction_date", "DATE", "", None),
            ("reference_number", "VARCHAR(100)", "", None),
            ("account_code", "VARCHAR(50)", "", None),
            ("cost_center_code", "VARCHAR(50)", "", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("debit_amount", "NUMERIC(15,2)", "", None),
            ("credit_amount", "NUMERIC(15,2)", "", None),
            ("description", "TEXT", "", None),
            ("created_by_user_id", "BIGINT", "FK", "users.id"),
            ("created_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "notifications": {
        "domain": "finance",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("user_id", "BIGINT", "FK", "users.id"),
            ("target_role", "VARCHAR(50)", "", None),
            ("target_organization_id", "BIGINT", "FK", "organizations.id"),
            ("type", "VARCHAR(50)", "", None),
            ("priority", "VARCHAR(50)", "", None),
            ("title", "VARCHAR(255)", "", None),
            ("message", "TEXT", "", None),
            ("reference_transaction_type", "VARCHAR(50)", "", None),
            ("reference_transaction_id", "BIGINT", "", None),
            ("action_url", "VARCHAR(255)", "", None),
            ("is_read", "BOOLEAN", "", None),
            ("read_at", "TIMESTAMPTZ", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
            ("updated_at", "TIMESTAMPTZ", "", None),
        ]
    },
    "audit_logs": {
        "domain": "finance",
        "cols": [
            ("id", "BIGSERIAL", "PK", None),
            ("user_id", "BIGINT", "FK", "users.id"),
            ("action", "VARCHAR(50)", "", None),
            ("auditable_type", "VARCHAR(100)", "", None),
            ("auditable_id", "BIGINT", "", None),
            ("organization_id", "BIGINT", "FK", "organizations.id"),
            ("ip_address", "VARCHAR(50)", "", None),
            ("user_agent", "TEXT", "", None),
            ("old_values", "JSONB", "", None),
            ("new_values", "JSONB", "", None),
            ("created_at", "TIMESTAMPTZ", "", None),
        ]
    }
}


# =============================================================================
# HELPER TO BUILD DRAW.IO XML
# =============================================================================

class DiagramBuilder:
    def __init__(self, page_id, page_name, width=4200, height=3200):
        self.page_id = page_id
        self.page_name = page_name
        self.width = width
        self.height = height
        self.cells = []
        self.cell_id_counter = 2
        self.table_cell_ids = {}

    def next_id(self):
        cid = f"{self.page_id}_{self.cell_id_counter}"
        self.cell_id_counter += 1
        return cid

    def add_title_block(self, title, subtitle):
        cid = self.next_id()
        val = (
            f"&lt;b style=&quot;font-size:22px;color:#0F172A;&quot;&gt;{html.escape(title)}&lt;/b&gt;&lt;br/&gt;"
            f"&lt;span style=&quot;font-size:13px;color:#475569;&quot;&gt;{html.escape(subtitle)}&lt;/span&gt;"
        )
        style = (
            "text;html=1;strokeColor=#CBD5E1;fillColor=#FFFFFF;align=left;verticalAlign=middle;"
            "rounded=1;shadow=1;spacingLeft=16;spacingRight=16;"
        )
        self.cells.append(
            f'<mxCell id="{cid}" value="{val}" style="{style}" vertex="1" parent="1">'
            f'<mxGeometry x="50" y="30" width="1100" height="70" as="geometry"/>'
            f'</mxCell>'
        )

    def add_legend_block(self, x, y):
        cid = self.next_id()
        val = (
            "&lt;b&gt;Legenda ERD:&lt;/b&gt;&amp;nbsp;&amp;nbsp;"
            "&lt;span style=&quot;background:#E11D48;color:#fff;padding:2px 4px;border-radius:3px;font-size:10px;&quot;&gt;PK&lt;/span&gt; Primary Key&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;"
            "&lt;span style=&quot;background:#2563EB;color:#fff;padding:2px 4px;border-radius:3px;font-size:10px;&quot;&gt;FK&lt;/span&gt; Foreign Key&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;"
            "&lt;span style=&quot;background:#D97706;color:#fff;padding:2px 4px;border-radius:3px;font-size:10px;&quot;&gt;UQ&lt;/span&gt; Unique Constraint&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;"
            "&lt;span style=&quot;color:#16A34A;font-weight:bold;&quot;&gt;—{ (Crow's Foot)&lt;/span&gt; Relasi 1-ke-Banyak (1..*)"
        )
        style = (
            "text;html=1;strokeColor=#E2E8F0;fillColor=#F8FAFC;align=left;verticalAlign=middle;"
            "rounded=1;fontSize=11;fontFamily=Segoe UI,Helvetica,Arial;spacingLeft=12;spacingRight=12;"
        )
        self.cells.append(
            f'<mxCell id="{cid}" value="{val}" style="{style}" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="920" height="36" as="geometry"/>'
            f'</mxCell>'
        )

    def add_zone_group(self, x, y, w, h, domain_key):
        domain = DOMAINS[domain_key]
        cid = self.next_id()
        val = f"&lt;b&gt;{html.escape(domain['title'])}&lt;/b&gt;"
        style = (
            f"rounded=1;whiteSpace=wrap;html=1;arcSize=3;fillColor={domain['fill']};"
            f"strokeColor={domain['border']};strokeWidth=2;dashed=1;verticalAlign=top;"
            f"fontStyle=1;fontSize=13;fontFamily=Segoe UI,Helvetica,Arial;fontColor={domain['header_font']};"
            f"align=left;spacingLeft=14;spacingTop=10;"
        )
        self.cells.append(
            f'<mxCell id="{cid}" value="{val}" style="{style}" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/>'
            f'</mxCell>'
        )
        return cid

    def add_table(self, table_name, x, y, width=280, max_cols=None):
        t_data = TABLES[table_name]
        d_data = DOMAINS[t_data["domain"]]
        cols = t_data["cols"]
        if max_cols and len(cols) > max_cols:
            visible_cols = cols[:max_cols]
            remaining = len(cols) - max_cols
        else:
            visible_cols = cols
            remaining = 0

        header_h = 30
        row_h = 24
        total_h = header_h + (len(visible_cols) * row_h) + (row_h if remaining > 0 else 0)

        t_cid = self.next_id()
        self.table_cell_ids[table_name] = t_cid

        # Table Swimlane Container
        t_val = f"&lt;b&gt;{html.escape(table_name)}&lt;/b&gt;"
        t_style = (
            f"swimlane;fontStyle=1;align=center;verticalAlign=top;childLayout=stackLayout;"
            f"horizontal=1;startSize={header_h};horizontalStack=0;resizeParent=1;resizeParentMax=0;"
            f"resizeLast=0;collapsible=1;marginBottom=0;whiteSpace=wrap;html=1;"
            f"fillColor={d_data['header_fill']};strokeColor={d_data['border']};"
            f"fontColor={d_data['header_font']};fontSize=12;fontFamily=Segoe UI,Helvetica,Arial;shadow=1;"
        )
        self.cells.append(
            f'<mxCell id="{t_cid}" value="{t_val}" style="{t_style}" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{width}" height="{total_h}" as="geometry"/>'
            f'</mxCell>'
        )

        # Rows
        curr_y = header_h
        for idx, (c_name, c_type, c_mod, ref) in enumerate(visible_cols):
            r_cid = self.next_id()
            row_bg = "#FFFFFF" if (idx % 2 == 0) else "#F9FAFB"

            if c_mod == "PK":
                tag = '&lt;font color=&quot;#E11D48&quot;&gt;&lt;b&gt;PK&lt;/b&gt;&lt;/font&gt;&amp;nbsp;'
            elif c_mod == "FK":
                tag = '&lt;font color=&quot;#2563EB&quot;&gt;&lt;b&gt;FK&lt;/b&gt;&lt;/font&gt;&amp;nbsp;'
            elif c_mod == "UQ":
                tag = '&lt;font color=&quot;#D97706&quot;&gt;&lt;b&gt;UQ&lt;/b&gt;&lt;/font&gt;&amp;nbsp;'
            else:
                tag = '&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;'

            r_val = f"{tag}&lt;b&gt;{html.escape(c_name)}&lt;/b&gt; : &lt;i style=&quot;color:#64748B;&quot;&gt;{html.escape(c_type)}&lt;/i&gt;"
            r_style = (
                f"text;strokeColor=none;fillColor={row_bg};align=left;verticalAlign=middle;"
                f"spacingLeft=8;spacingRight=8;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];"
                f"portConstraint=eastwest;whiteSpace=wrap;html=1;fontSize=10;fontFamily=Consolas,monospace;"
            )
            self.cells.append(
                f'<mxCell id="{r_cid}" value="{r_val}" style="{r_style}" vertex="1" parent="{t_cid}">'
                f'<mxGeometry y="{curr_y}" width="{width}" height="{row_h}" as="geometry"/>'
                f'</mxCell>'
            )
            curr_y += row_h

        if remaining > 0:
            r_cid = self.next_id()
            r_val = f"&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&lt;i style=&quot;color:#94A3B8;&quot;&gt;... +{remaining} kolom lainnya&lt;/i&gt;"
            r_style = (
                f"text;strokeColor=none;fillColor=#F1F5F9;align=left;verticalAlign=middle;"
                f"spacingLeft=8;spacingRight=8;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];"
                f"portConstraint=eastwest;whiteSpace=wrap;html=1;fontSize=10;fontFamily=Segoe UI,Arial;"
            )
            self.cells.append(
                f'<mxCell id="{r_cid}" value="{r_val}" style="{r_style}" vertex="1" parent="{t_cid}">'
                f'<mxGeometry y="{curr_y}" width="{width}" height="{row_h}" as="geometry"/>'
                f'</mxCell>'
            )

        return t_cid

    def add_relationship(self, from_table, to_table, label="", edge_color=None):
        if from_table not in self.table_cell_ids or to_table not in self.table_cell_ids:
            return
        from_id = self.table_cell_ids[from_table]
        to_id = self.table_cell_ids[to_table]
        if not edge_color:
            edge_color = DOMAINS[TABLES[from_table]["domain"]]["edge_color"]

        cid = self.next_id()
        # Crow's foot notation: source is Many (ERmany), target is One (ERmandOne)
        style = (
            f"edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;html=1;"
            f"strokeColor={edge_color};strokeWidth=1.5;fontSize=10;fontFamily=Segoe UI,Arial;"
            f"endArrow=ERmandOne;startArrow=ERmany;exitPerimeter=1;entryPerimeter=1;"
        )
        self.cells.append(
            f'<mxCell id="{cid}" value="{html.escape(label)}" style="{style}" edge="1" parent="1" source="{from_id}" target="{to_id}">'
            f'<mxGeometry relative="1" as="geometry"/>'
            f'</mxCell>'
        )

    def to_xml(self):
        cells_str = "\n      ".join(self.cells)
        return (
            f'  <diagram id="{self.page_id}" name="{html.escape(self.page_name)}">\n'
            f'    <mxGraphModel dx="1600" dy="1000" grid="1" gridSize="10" guides="1" tooltips="1" '
            f'connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{self.width}" pageHeight="{self.height}" '
            f'background="#F8FAFC" math="0" shadow="0">\n'
            f'      <root>\n'
            f'        <mxCell id="0"/>\n'
            f'        <mxCell id="1" parent="0"/>\n'
            f'      {cells_str}\n'
            f'      </root>\n'
            f'    </mxGraphModel>\n'
            f'  </diagram>'
        )


# =============================================================================
# BUILD PAGE 1: FULL ENTERPRISE ERD (ALL 45 TABLES)
# =============================================================================

def build_full_enterprise_diagram():
    diag = DiagramBuilder(
        "page_full_erd", 
        "1. Full Enterprise ERD (45 Tables)", 
        width=4800, 
        height=3800
    )
    diag.add_title_block(
        "Bank Jatim - JIMS (Jatim Inventory Management System)",
        "Enterprise Architecture Entity Relationship Diagram • 45 Tables • Multi-Bucket Stock Engine, Procurement Consolidation, ATM Emboss & GL"
    )
    diag.add_legend_block(1200, 48)

    # -------------------------------------------------------------------------
    # DOMAIN ZONES LAYOUT (Grid 4 Columns x 2 Rows of Zones)
    # -------------------------------------------------------------------------

    # ZONE 1: Master Data (Col 1, Row 1)
    diag.add_zone_group(50, 130, 1100, 1450, "master")
    diag.add_table("organizations", 80, 180, width=280)
    diag.add_table("warehouses", 420, 180, width=260)
    diag.add_table("users", 740, 180, width=280)
    diag.add_table("categories", 80, 580, width=280)
    diag.add_table("items", 420, 580, width=280)
    diag.add_table("item_conversions", 760, 580, width=260)
    diag.add_table("vendors", 80, 1080, width=280)
    diag.add_table("couriers", 420, 1080, width=260)
    diag.add_table("expedition_mappings", 740, 1080, width=280)

    # ZONE 2: Procurement & PR-PO Consolidation (Col 2, Row 1)
    diag.add_zone_group(1200, 130, 1150, 1450, "procurement")
    diag.add_table("budgets", 1230, 180, width=280)
    diag.add_table("purchase_requests", 1580, 180, width=310)
    diag.add_table("purchase_request_items", 1960, 180, width=310)
    diag.add_table("purchase_orders", 1230, 680, width=320)
    diag.add_table("purchase_order_items", 1630, 680, width=320)
    diag.add_table("goods_receipts", 1230, 1150, width=320)
    diag.add_table("goods_receipt_items", 1630, 1150, width=320)

    # ZONE 3: Branch Orders & Requisitions (Col 3, Row 1)
    diag.add_zone_group(2400, 130, 1150, 1450, "orders")
    diag.add_table("orders", 2430, 180, width=320)
    diag.add_table("order_items", 2830, 180, width=320)
    diag.add_table("order_allocations", 3220, 180, width=300)
    diag.add_table("switching_stocks", 2430, 880, width=320)
    diag.add_table("switching_stock_items", 2830, 880, width=310)

    # ZONE 4: Warehouse Fulfillment & Distribution (Col 4, Row 1)
    diag.add_zone_group(3600, 130, 1150, 1450, "logistics")
    diag.add_table("warehouse_pickings", 3630, 180, width=280)
    diag.add_table("warehouse_packings", 3980, 180, width=280)
    diag.add_table("shipments", 3630, 520, width=320)
    diag.add_table("receivings", 3630, 1020, width=320)
    diag.add_table("discrepancies", 4020, 1020, width=320)

    # ZONE 5: Stock Engine & Balances (Col 1, Row 2)
    diag.add_zone_group(50, 1630, 1100, 1350, "inventory")
    diag.add_table("stock_balances", 80, 1680, width=320)
    diag.add_table("stock_ledgers", 450, 1680, width=320)
    diag.add_table("stock_adjustments", 80, 2100, width=320)
    diag.add_table("stock_opnames", 450, 2100, width=300)
    diag.add_table("stock_opname_items", 800, 2100, width=300)

    # ZONE 6: ATM Card Personalization - Emboss (Col 2, Row 2)
    diag.add_zone_group(1200, 1630, 1150, 1350, "emboss")
    diag.add_table("emboss_files", 1230, 1680, width=300)
    diag.add_table("emboss_records", 1600, 1680, width=330)
    diag.add_table("production_orders", 1230, 2100, width=300)
    diag.add_table("production_order_items", 1600, 2100, width=300)

    # ZONE 7: Reverse Logistics & Destruction (Col 3, Row 2)
    diag.add_zone_group(2400, 1630, 1150, 1350, "reverse")
    diag.add_table("inventory_returns", 2430, 1680, width=310)
    diag.add_table("inventory_return_items", 2820, 1680, width=300)
    diag.add_table("stock_destructions", 2430, 2100, width=310)
    diag.add_table("stock_destruction_items", 2820, 2100, width=310)

    # ZONE 8: Finance Settlements, GL & Audit (Col 4, Row 2)
    diag.add_zone_group(3600, 1630, 1150, 1350, "finance")
    diag.add_table("settlements", 3630, 1680, width=320)
    diag.add_table("chart_of_accounts", 4020, 1680, width=280)
    diag.add_table("cost_centers", 4020, 1980, width=280)
    diag.add_table("general_ledger_entries", 3630, 2200, width=320)
    diag.add_table("notifications", 4020, 2250, width=320)
    diag.add_table("audit_logs", 3630, 2580, width=320)

    # -------------------------------------------------------------------------
    # RELATIONSHIPS (FOREIGN KEYS)
    # -------------------------------------------------------------------------
    # Master
    diag.add_relationship("organizations", "organizations", "parent_id")
    diag.add_relationship("warehouses", "organizations", "organization_id")
    diag.add_relationship("users", "organizations", "organization_id")
    diag.add_relationship("users", "warehouses", "warehouse_id")
    diag.add_relationship("items", "categories", "category_id")
    diag.add_relationship("item_conversions", "items", "item_id")
    diag.add_relationship("expedition_mappings", "organizations", "destination_organization_id")
    diag.add_relationship("expedition_mappings", "couriers", "courier_id")

    # Procurement
    diag.add_relationship("budgets", "organizations", "organization_id")
    diag.add_relationship("purchase_requests", "organizations", "organization_id")
    diag.add_relationship("purchase_requests", "users", "created_by")
    diag.add_relationship("purchase_request_items", "purchase_requests", "purchase_request_id")
    diag.add_relationship("purchase_request_items", "items", "item_id")
    diag.add_relationship("purchase_orders", "vendors", "vendor_id")
    diag.add_relationship("purchase_orders", "warehouses", "warehouse_id")
    diag.add_relationship("purchase_orders", "users", "created_by")
    diag.add_relationship("purchase_order_items", "purchase_orders", "purchase_order_id")
    diag.add_relationship("purchase_order_items", "items", "item_id")
    diag.add_relationship("purchase_order_items", "purchase_request_items", "pr_item_id")
    diag.add_relationship("goods_receipts", "purchase_orders", "purchase_order_id")
    diag.add_relationship("goods_receipts", "warehouses", "warehouse_id")
    diag.add_relationship("goods_receipt_items", "goods_receipts", "goods_receipt_id")
    diag.add_relationship("goods_receipt_items", "purchase_order_items", "po_item_id")
    diag.add_relationship("goods_receipt_items", "items", "item_id")

    # Orders & Allocations
    diag.add_relationship("orders", "organizations", "requesting_org")
    diag.add_relationship("orders", "warehouses", "requesting_wh")
    diag.add_relationship("orders", "users", "created_by")
    diag.add_relationship("order_items", "orders", "order_id")
    diag.add_relationship("order_items", "items", "item_id")
    diag.add_relationship("order_allocations", "order_items", "order_item_id")
    diag.add_relationship("order_allocations", "warehouses", "source_wh")
    diag.add_relationship("order_allocations", "switching_stocks", "switching_stock_id")
    diag.add_relationship("switching_stocks", "orders", "order_id")
    diag.add_relationship("switching_stocks", "organizations", "source_org")
    diag.add_relationship("switching_stocks", "warehouses", "source_wh")
    diag.add_relationship("switching_stock_items", "switching_stocks", "switching_stock_id")
    diag.add_relationship("switching_stock_items", "items", "item_id")

    # Logistics
    diag.add_relationship("warehouse_pickings", "orders", "order_id")
    diag.add_relationship("warehouse_pickings", "warehouses", "warehouse_id")
    diag.add_relationship("warehouse_packings", "orders", "order_id")
    diag.add_relationship("warehouse_packings", "warehouses", "warehouse_id")
    diag.add_relationship("shipments", "orders", "order_id")
    diag.add_relationship("shipments", "switching_stocks", "switching_stock_id")
    diag.add_relationship("shipments", "warehouses", "origin_wh")
    diag.add_relationship("shipments", "organizations", "dest_org")
    diag.add_relationship("shipments", "couriers", "courier_id")
    diag.add_relationship("receivings", "shipments", "shipment_id")
    diag.add_relationship("receivings", "orders", "order_id")
    diag.add_relationship("receivings", "warehouses", "warehouse_id")
    diag.add_relationship("discrepancies", "receivings", "receiving_id")
    diag.add_relationship("discrepancies", "order_items", "order_item_id")
    diag.add_relationship("discrepancies", "items", "item_id")

    # Stock Engine
    diag.add_relationship("stock_balances", "warehouses", "warehouse_id")
    diag.add_relationship("stock_balances", "items", "item_id")
    diag.add_relationship("stock_ledgers", "warehouses", "warehouse_id")
    diag.add_relationship("stock_ledgers", "items", "item_id")
    diag.add_relationship("stock_adjustments", "warehouses", "warehouse_id")
    diag.add_relationship("stock_adjustments", "items", "item_id")
    diag.add_relationship("stock_opnames", "warehouses", "warehouse_id")
    diag.add_relationship("stock_opname_items", "stock_opnames", "opname_id")
    diag.add_relationship("stock_opname_items", "items", "item_id")

    # Emboss
    diag.add_relationship("emboss_files", "users", "uploaded_by")
    diag.add_relationship("emboss_records", "emboss_files", "emboss_file_id")
    diag.add_relationship("emboss_records", "items", "item_id")
    diag.add_relationship("emboss_records", "orders", "order_id")
    diag.add_relationship("production_orders", "warehouses", "warehouse_id")
    diag.add_relationship("production_order_items", "production_orders", "prod_order_id")
    diag.add_relationship("production_order_items", "items", "item_id")

    # Reverse
    diag.add_relationship("inventory_returns", "organizations", "organization_id")
    diag.add_relationship("inventory_returns", "warehouses", "dest_wh")
    diag.add_relationship("inventory_return_items", "inventory_returns", "return_id")
    diag.add_relationship("inventory_return_items", "items", "item_id")
    diag.add_relationship("stock_destructions", "warehouses", "warehouse_id")
    diag.add_relationship("stock_destruction_items", "stock_destructions", "destruction_id")
    diag.add_relationship("stock_destruction_items", "items", "item_id")

    # Finance
    diag.add_relationship("settlements", "orders", "order_id")
    diag.add_relationship("settlements", "organizations", "debit_org")
    diag.add_relationship("general_ledger_entries", "organizations", "organization_id")
    diag.add_relationship("notifications", "users", "user_id")
    diag.add_relationship("notifications", "organizations", "target_org")
    diag.add_relationship("audit_logs", "users", "user_id")

    return diag


# =============================================================================
# BUILD PAGE 2: CORE BUSINESS TRANSACTION FLOW (16 CRITICAL TABLES)
# =============================================================================

def build_core_flow_diagram():
    diag = DiagramBuilder(
        "page_core_flow", 
        "2. Core Business Transaction Flow", 
        width=3200, 
        height=2200
    )
    diag.add_title_block(
        "Alur Transaksi Utama (Core Supply Chain Lifecycle)",
        "PR/PO Pengadaan Konsolidasian ➔ Saldo Stok & Kartu Stok ➔ Order Cabang ➔ Picking/Packing ➔ Ekspedisi ➔ Receiving & BAP ➔ Settlement GL"
    )
    diag.add_legend_block(950, 48)

    # Core tables arranged in 4 stages
    # Stage 1: Master & Foundation
    diag.add_table("organizations", 80, 160, width=280)
    diag.add_table("warehouses", 80, 600, width=280)
    diag.add_table("users", 80, 980, width=280)
    diag.add_table("items", 80, 1500, width=280)

    # Stage 2: Procurement to Stock
    diag.add_table("purchase_requests", 480, 160, width=320)
    diag.add_table("purchase_orders", 480, 720, width=320)
    diag.add_table("goods_receipts", 480, 1280, width=320)
    diag.add_table("stock_balances", 920, 720, width=320)
    diag.add_table("stock_ledgers", 920, 1280, width=320)

    # Stage 3: Branch Ordering & Fulfillment
    diag.add_table("orders", 1360, 160, width=330)
    diag.add_table("order_items", 1360, 800, width=320)
    diag.add_table("warehouse_pickings", 1820, 160, width=280)
    diag.add_table("shipments", 1820, 600, width=320)
    diag.add_table("receivings", 1820, 1150, width=320)

    # Stage 4: Financial Settlement & GL
    diag.add_table("settlements", 2280, 160, width=320)
    diag.add_table("general_ledger_entries", 2280, 680, width=320)

    # Flow Relations
    diag.add_relationship("warehouses", "organizations", "belongs to")
    diag.add_relationship("users", "organizations", "assigned org")
    diag.add_relationship("purchase_requests", "organizations", "requesting unit")
    diag.add_relationship("purchase_orders", "warehouses", "delivery warehouse")
    diag.add_relationship("goods_receipts", "purchase_orders", "receipt of PO")
    diag.add_relationship("goods_receipts", "warehouses", "received in")
    diag.add_relationship("stock_balances", "warehouses", "stock per wh")
    diag.add_relationship("stock_balances", "items", "stock per item")
    diag.add_relationship("stock_ledgers", "stock_balances", "audit mutasi")
    diag.add_relationship("orders", "organizations", "ordered by branch")
    diag.add_relationship("order_items", "orders", "items requested")
    diag.add_relationship("order_items", "items", "item SKU")
    diag.add_relationship("warehouse_pickings", "orders", "pick execution")
    diag.add_relationship("shipments", "orders", "manifest shipping")
    diag.add_relationship("receivings", "shipments", "POD proof of delivery")
    diag.add_relationship("settlements", "orders", "inter-unit billing")
    diag.add_relationship("general_ledger_entries", "organizations", "accounting journal")

    return diag


# =============================================================================
# BUILD PAGE 3: STOCK ENGINE & INVENTORY CONTROL
# =============================================================================

def build_inventory_diagram():
    diag = DiagramBuilder(
        "page_stock_engine", 
        "3. Stock Engine & Multi-Bucket Balance", 
        width=2600, 
        height=1800
    )
    diag.add_title_block(
        "Stock Balance Engine & Multi-Bucket Formula",
        "Available = On Hand - (Reserved + Allocated + Hold + Damaged) • Double-Entry Immutable Stock Ledger • Stock Opname & Penyesuaian"
    )

    diag.add_table("warehouses", 80, 160, width=280)
    diag.add_table("categories", 80, 560, width=280)
    diag.add_table("items", 80, 920, width=300)
    diag.add_table("item_conversions", 80, 1420, width=280)

    diag.add_table("stock_balances", 520, 160, width=340)
    diag.add_table("stock_ledgers", 520, 700, width=340)
    diag.add_table("stock_adjustments", 520, 1200, width=340)

    diag.add_table("stock_opnames", 980, 160, width=320)
    diag.add_table("stock_opname_items", 980, 600, width=320)
    diag.add_table("stock_destructions", 980, 1080, width=320)
    diag.add_table("stock_destruction_items", 980, 1500, width=320)

    diag.add_relationship("items", "categories", "category_id")
    diag.add_relationship("item_conversions", "items", "item_id")
    diag.add_relationship("stock_balances", "warehouses", "warehouse_id")
    diag.add_relationship("stock_balances", "items", "item_id")
    diag.add_relationship("stock_ledgers", "warehouses", "warehouse_id")
    diag.add_relationship("stock_ledgers", "items", "item_id")
    diag.add_relationship("stock_adjustments", "warehouses", "warehouse_id")
    diag.add_relationship("stock_adjustments", "items", "item_id")
    diag.add_relationship("stock_opnames", "warehouses", "warehouse_id")
    diag.add_relationship("stock_opname_items", "stock_opnames", "stock_opname_id")
    diag.add_relationship("stock_opname_items", "items", "item_id")
    diag.add_relationship("stock_destructions", "warehouses", "warehouse_id")
    diag.add_relationship("stock_destruction_items", "stock_destructions", "stock_destruction_id")
    diag.add_relationship("stock_destruction_items", "items", "item_id")

    return diag


# =============================================================================
# BUILD PAGE 4: PROCUREMENT & PR-PO CONSOLIDATION
# =============================================================================

def build_procurement_diagram():
    diag = DiagramBuilder(
        "page_procurement", 
        "4. Procurement & Approved PR Consolidation", 
        width=2800, 
        height=1800
    )
    diag.add_title_block(
        "Modul Pengadaan & Konsolidasi PR ke PO Masal",
        "Pagu Anggaran Unit Kerja ➔ Pool Approved PR Antar Cabang ➔ Purchase Order Konsolidasi Vendor ➔ Good Receipt Note (GRN) & QC"
    )

    diag.add_table("organizations", 80, 160, width=280)
    diag.add_table("budgets", 80, 600, width=280)
    diag.add_table("vendors", 80, 1020, width=280)

    diag.add_table("purchase_requests", 480, 160, width=320)
    diag.add_table("purchase_request_items", 480, 720, width=320)

    diag.add_table("purchase_orders", 920, 160, width=330)
    diag.add_table("purchase_order_items", 920, 720, width=330)

    diag.add_table("goods_receipts", 1380, 160, width=320)
    diag.add_table("goods_receipt_items", 1380, 680, width=320)

    diag.add_relationship("budgets", "organizations", "pagu organisasi")
    diag.add_relationship("purchase_requests", "organizations", "pemohon")
    diag.add_relationship("purchase_request_items", "purchase_requests", "pr_id")
    diag.add_relationship("purchase_orders", "vendors", "vendor mitra")
    diag.add_relationship("purchase_order_items", "purchase_orders", "po_id")
    diag.add_relationship("purchase_order_items", "purchase_request_items", "konsolidasi PR")
    diag.add_relationship("goods_receipts", "purchase_orders", "po_id")
    diag.add_relationship("goods_receipt_items", "goods_receipts", "grn_id")
    diag.add_relationship("goods_receipt_items", "purchase_order_items", "po_item_id")

    return diag


# =============================================================================
# BUILD PAGE 5: DISTRIBUTION, LOGISTICS & EMBOSS
# =============================================================================

def build_logistics_emboss_diagram():
    diag = DiagramBuilder(
        "page_logistics_emboss", 
        "5. Distribution, Logistics & Card Emboss", 
        width=3000, 
        height=2000
    )
    diag.add_title_block(
        "Distribusi Cabang, Ekspedisi & Personalisasi Kartu ATM (Emboss)",
        "Pengalihan Stok Antar-Cabang (Switching) • Picking & Packing Koli • Barcode Resi Pengiriman • File Parser Core Banking & BAP"
    )

    diag.add_table("orders", 80, 160, width=320)
    diag.add_table("order_items", 80, 780, width=320)
    diag.add_table("order_allocations", 80, 1280, width=320)

    diag.add_table("switching_stocks", 500, 160, width=320)
    diag.add_table("switching_stock_items", 500, 720, width=320)
    diag.add_table("couriers", 500, 1180, width=280)
    diag.add_table("expedition_mappings", 500, 1560, width=280)

    diag.add_table("warehouse_pickings", 940, 160, width=280)
    diag.add_table("warehouse_packings", 940, 520, width=280)
    diag.add_table("shipments", 940, 920, width=320)
    diag.add_table("receivings", 940, 1480, width=320)
    diag.add_table("discrepancies", 1360, 1480, width=320)

    diag.add_table("emboss_files", 1360, 160, width=320)
    diag.add_table("emboss_records", 1360, 600, width=330)
    diag.add_table("production_orders", 1780, 160, width=300)
    diag.add_table("production_order_items", 1780, 580, width=300)

    diag.add_relationship("order_items", "orders", "order_id")
    diag.add_relationship("order_allocations", "order_items", "order_item_id")
    diag.add_relationship("order_allocations", "switching_stocks", "switching_stock_id")
    diag.add_relationship("switching_stocks", "orders", "order_id")
    diag.add_relationship("switching_stock_items", "switching_stocks", "switching_stock_id")
    diag.add_relationship("warehouse_pickings", "orders", "order_id")
    diag.add_relationship("warehouse_packings", "orders", "order_id")
    diag.add_relationship("shipments", "orders", "order_id")
    diag.add_relationship("shipments", "couriers", "courier_id")
    diag.add_relationship("receivings", "shipments", "shipment_id")
    diag.add_relationship("discrepancies", "receivings", "receiving_id")
    diag.add_relationship("emboss_records", "emboss_files", "emboss_file_id")
    diag.add_relationship("emboss_records", "orders", "order_id")
    diag.add_relationship("production_order_items", "production_orders", "production_order_id")

    return diag


# =============================================================================
# MAIN EXPORT ROUTINE
# =============================================================================

def generate_drawio_xml():
    p1 = build_full_enterprise_diagram()
    p2 = build_core_flow_diagram()
    p3 = build_inventory_diagram()
    p4 = build_procurement_diagram()
    p5 = build_logistics_emboss_diagram()

    xml_content = (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        '<mxfile host="Electron" modified="2026-09-13T12:00:00.000Z" agent="5.0" version="21.0.0" type="device">\n'
        f'{p1.to_xml()}\n'
        f'{p2.to_xml()}\n'
        f'{p3.to_xml()}\n'
        f'{p4.to_xml()}\n'
        f'{p5.to_xml()}\n'
        '</mxfile>'
    )
    return xml_content


def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(base_dir, ".."))

    target_docs = os.path.join(base_dir, "bank_jatim_erd.drawio")
    target_root = os.path.join(project_root, "erd.drawio")

    print(f"Generating Bank Jatim JIMS ERD Draw.io diagrams...")
    xml_data = generate_drawio_xml()

    with open(target_docs, "w", encoding="utf-8") as f:
        f.write(xml_data)
    print(f" Saved: {target_docs} ({len(xml_data)} bytes)")

    with open(target_root, "w", encoding="utf-8") as f:
        f.write(xml_data)
    print(f" Saved: {target_root} ({len(xml_data)} bytes)")


if __name__ == "__main__":
    main()
