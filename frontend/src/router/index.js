import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useNavigationStore } from '@/stores/navigation';
import AppLayout from '@/components/AppLayout.vue';
import { createNavigationGuard } from '@/router/navigationGuard';

const routes = [
  // -------------------------------------------------------------
  // Public Auth Pages (Standalone without sidebar/navbar)
  // -------------------------------------------------------------
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    meta: { public: true }
  },
  {
    path: '/access-denied',
    name: 'access-denied',
    component: () => import('@/views/errors/AccessDeniedView.vue'),
    meta: { system: true }
  },

  // -------------------------------------------------------------
  // Standalone Print & Document Views (Without sidebar/navbar for clean printing)
  // -------------------------------------------------------------
  {
    path: '/orders/:id/print',
    name: 'orders-print',
    component: () => import('@/views/orders/OrderPrintView.vue')
  },
  {
    path: '/procurement/pr/:id/print',
    name: 'procurement-pr-print',
    component: () => import('@/views/procurement/PurchaseRequestPrintView.vue')
  },
  {
    path: '/procurement/po/:id/print',
    name: 'procurement-po-print',
    component: () => import('@/views/procurement/PurchaseOrderPrintView.vue')
  },
  {
    path: '/distribution/manifest/:id/print',
    name: 'distribution-manifest-print',
    component: () => import('@/views/distribution/ManifestPrintView.vue')
  },
  {
    path: '/distribution/label/:id/print',
    name: 'distribution-label-print',
    component: () => import('@/views/distribution/LabelPrintView.vue')
  },
  {
    path: '/production/:id/manifest',
    name: 'production-manifest',
    component: () => import('@/views/production/ProductionManifestView.vue')
  },
  {
    path: '/destructions/:id/berita-acara',
    name: 'destructions-berita-acara',
    component: () => import('@/views/destructions/DestructionBeritaAcaraView.vue')
  },
  {
    path: '/receiving/discrepancies/:id/print',
    name: 'receiving-discrepancy-print',
    component: () => import('@/views/receiving/DiscrepancyPrintView.vue')
  },
  {
    path: '/receiving/discrepancies/:id/berita-acara',
    name: 'receiving-discrepancy-ba',
    component: () => import('@/views/receiving/DiscrepancyPrintView.vue')
  },

  // -------------------------------------------------------------
  // Main In-App Routes (Under AppLayout with AdminLTE Header & Sidebar)
  // -------------------------------------------------------------
  {
    path: '/',
    component: AppLayout,
    children: [
      // 1. Dashboard Overview
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/OperationalDashboardView.vue')
      },
      {
        path: 'dashboard/operational',
        name: 'operational-dashboard',
        component: () => import('@/views/dashboard/OperationalDashboardView.vue')
      },
      {
        path: 'dashboard/executive',
        name: 'executive-dashboard',
        component: () => import('@/views/dashboard/ExecutiveDashboardView.vue')
      },

      // 2. Permintaan & Order
      {
        path: 'orders',
        name: 'orders-index',
        component: () => import('@/views/orders/BranchOrdersView.vue')
      },
      {
        path: 'orders/branch',
        name: 'orders-branch',
        component: () => import('@/views/orders/BranchOrdersView.vue')
      },
      {
        path: 'orders/create',
        name: 'orders-create',
        component: () => import('@/views/orders/CreateOrderView.vue')
      },
      {
        path: 'orders/approvals',
        name: 'orders-approvals',
        component: () => import('@/views/orders/OrderApprovalView.vue')
      },
      {
        path: 'orders/:id',
        name: 'orders-detail',
        component: () => import('@/views/orders/OrderDetailView.vue')
      },
      {
        path: 'emboss',
        name: 'emboss-index',
        component: () => import('@/views/emboss/CardPersonalizationView.vue')
      },
      {
        path: 'emboss/cards',
        name: 'emboss-cards',
        component: () => import('@/views/emboss/CardPersonalizationView.vue')
      },
      {
        path: 'emboss/reject-queue',
        name: 'emboss-reject-queue',
        component: () => import('@/views/emboss/EmbossRejectQueueView.vue')
      },
      {
        path: 'emboss/:id',
        name: 'emboss-detail',
        component: () => import('@/views/emboss/EmbossDetailView.vue')
      },
      {
        path: 'production',
        name: 'production-index',
        component: () => import('@/views/production/ProductionIndexView.vue')
      },
      {
        path: 'production/:id',
        name: 'production-detail',
        component: () => import('@/views/production/ProductionDetailView.vue')
      },

      // 3. Gudang & Distribusi
      {
        path: 'warehouse/picking',
        name: 'warehouse-picking',
        component: () => import('@/views/warehouse/PickingQueueView.vue')
      },
      {
        path: 'warehouse/packing',
        name: 'warehouse-packing',
        component: () => import('@/views/warehouse/PackingQueueView.vue')
      },
      {
        path: 'warehouse/picking-packing',
        name: 'warehouse-picking-packing',
        component: () => import('@/views/warehouse/PickingPackingView.vue')
      },
      {
        path: 'distribution/shipments',
        name: 'distribution-shipments',
        component: () => import('@/views/distribution/ShipmentsView.vue')
      },
      {
        path: 'distribution/shipments/:id',
        name: 'distribution-shipment-detail',
        component: () => import('@/views/distribution/ShipmentsView.vue')
      },

      // 4. Penerimaan (Receiving)
      {
        path: 'receiving/po',
        name: 'receiving-po',
        component: () => import('@/views/receiving/ReceivingPoView.vue')
      },
      {
        path: 'receiving',
        name: 'receiving-branch',
        component: () => import('@/views/receiving/ReceivingBranchView.vue')
      },
      {
        path: 'receiving/confirmation',
        name: 'receiving-confirmation',
        component: () => import('@/views/receiving/ReceivingConfirmationView.vue')
      },
      {
        path: 'receiving/confirm/:id',
        name: 'receiving-confirm-detail',
        component: () => import('@/views/receiving/ReceivingConfirmationView.vue')
      },
      {
        path: 'receiving/discrepancies',
        name: 'receiving-discrepancies',
        component: () => import('@/views/receiving/DiscrepanciesView.vue')
      },

      // 5. Persediaan (Inventory)
      {
        path: 'inventory/balances',
        name: 'inventory-balances',
        component: () => import('@/views/inventory/StockBalancesView.vue')
      },
      {
        path: 'inventory/stock-balances',
        name: 'inventory-stock-balances',
        component: () => import('@/views/inventory/StockBalancesView.vue')
      },
      {
        path: 'inventory/stock-card',
        name: 'inventory-stock-card',
        component: () => import('@/views/inventory/StockLedgerCardView.vue')
      },
      {
        path: 'inventory/stock-card/:itemId',
        name: 'inventory-stock-card-detail',
        component: () => import('@/views/inventory/StockLedgerCardView.vue')
      },
      {
        path: 'inventory/ledger',
        name: 'inventory-ledger',
        component: () => import('@/views/inventory/StockLedgerCardView.vue')
      },
      {
        path: 'inventory/reconciliation',
        name: 'inventory-reconciliation',
        component: () => import('@/views/inventory/ReconciliationView.vue')
      },
      {
        path: 'inventory/movement-inquiry',
        name: 'inventory-movement-inquiry',
        component: () => import('@/views/inventory/StockLedgerCardView.vue')
      },
      {
        path: 'inventory/initial-stock',
        name: 'inventory-initial-stock',
        component: () => import('@/views/inventory/InitialStockView.vue')
      },
      {
        path: 'inventory/initial-stock/history',
        name: 'inventory-initial-stock-history',
        component: () => import('@/views/inventory/InitialStockHistoryView.vue')
      },
      {
        path: 'inventory/stock-opname',
        name: 'inventory-stock-opname',
        component: () => import('@/views/inventory/StockOpnameView.vue')
      },
      {
        path: 'inventory/stock-opname/history',
        name: 'inventory-stock-opname-history',
        component: () => import('@/views/inventory/StockOpnameHistoryView.vue')
      },
      {
        path: 'inventory/stock-opname/history/:id',
        name: 'inventory-stock-opname-history-detail',
        component: () => import('@/views/inventory/StockOpnameHistoryView.vue')
      },
      {
        path: 'inventory/early-warning',
        name: 'inventory-early-warning',
        component: () => import('@/views/inventory/EarlyWarningSystemView.vue')
      },
      {
        path: 'inventory/ews',
        name: 'inventory-ews',
        component: () => import('@/views/inventory/EarlyWarningSystemView.vue')
      },
      {
        path: 'inventory/forecasting',
        name: 'inventory-forecasting',
        component: () => import('@/views/inventory/ForecastingView.vue')
      },
      {
        path: 'inventory/switching',
        name: 'inventory-switching',
        component: () => import('@/views/inventory/SwitchingStockView.vue')
      },
      {
        path: 'inventory/switching-stocks',
        name: 'inventory-switching-stocks',
        component: () => import('@/views/inventory/SwitchingStockView.vue')
      },
      {
        path: 'inventory/switching/approvals',
        name: 'inventory-switching-approvals',
        component: () => import('@/views/inventory/SwitchingApprovalView.vue')
      },
      {
        path: 'inventory/switching-stocks/approvals',
        name: 'inventory-switching-stocks-approvals',
        component: () => import('@/views/inventory/SwitchingApprovalView.vue')
      },
      {
        path: 'returns',
        name: 'returns-index',
        component: () => import('@/views/returns/ReturnsView.vue')
      },
      {
        path: 'returns/create',
        name: 'returns-create',
        component: () => import('@/views/returns/CreateReturnView.vue')
      },
      {
        path: 'returns/:id',
        name: 'returns-detail',
        component: () => import('@/views/returns/ReturnDetailView.vue')
      },
      {
        path: 'destructions',
        name: 'destructions-index',
        component: () => import('@/views/destructions/DestructionsView.vue')
      },
      {
        path: 'destructions/create',
        name: 'destructions-create',
        component: () => import('@/views/destructions/CreateDestructionView.vue')
      },
      {
        path: 'destructions/:id',
        name: 'destructions-detail',
        component: () => import('@/views/destructions/DestructionDetailView.vue')
      },

      // 6. Pengadaan (Procurement)
      {
        path: 'procurement/pr',
        name: 'procurement-pr',
        component: () => import('@/views/procurement/PurchaseRequestsView.vue')
      },
      {
        path: 'procurement/pr/:id',
        name: 'procurement-pr-detail',
        component: () => import('@/views/procurement/PurchaseRequestDetailView.vue')
      },
      {
        path: 'procurement/approvals/pr',
        name: 'procurement-approvals-pr',
        component: () => import('@/views/procurement/PurchaseRequestApprovalView.vue')
      },
      {
        path: 'procurement/consolidation',
        name: 'procurement-consolidation',
        component: () => import('@/views/procurement/PrConsolidationView.vue')
      },
      {
        path: 'procurement/po',
        name: 'procurement-po',
        component: () => import('@/views/procurement/PurchaseOrdersView.vue')
      },
      {
        path: 'procurement/po/:id',
        name: 'procurement-po-detail',
        component: () => import('@/views/procurement/PurchaseOrdersView.vue')
      },
      {
        path: 'procurement/approvals/po',
        name: 'procurement-approvals-po',
        component: () => import('@/views/procurement/PurchaseOrderApprovalView.vue')
      },

      // 7. Finance
      {
        path: 'finance/settlements',
        name: 'finance-settlements',
        component: () => import('@/views/finance/SettlementsView.vue')
      },

      // 8. Master Data
      {
        path: 'master/organizations',
        name: 'master-organizations',
        component: () => import('@/views/master/OrganizationsView.vue')
      },
      {
        path: 'master/items',
        name: 'master-items',
        component: () => import('@/views/master/ItemsView.vue')
      },
      {
        path: 'master/budgets',
        name: 'master-budgets',
        component: () => import('@/views/master/BudgetsView.vue')
      },
      {
        path: 'master/budgets/early-warning',
        name: 'master-budgets-early-warning',
        component: () => import('@/views/master/BudgetsEarlyWarningView.vue')
      },
      {
        path: 'master/accounting',
        name: 'master-accounting',
        component: () => import('@/views/master/AccountingView.vue')
      },
      {
        path: 'master/vendors',
        name: 'master-vendors',
        component: () => import('@/views/master/VendorsCouriersView.vue')
      },
      {
        path: 'master/vendors-couriers',
        name: 'master-vendors-couriers',
        component: () => import('@/views/master/VendorsCouriersView.vue')
      },
      {
        path: 'master/expedition-mappings',
        name: 'master-expedition-mappings',
        component: () => import('@/views/master/ExpeditionMappingsView.vue')
      },
      {
        path: 'master/users',
        name: 'master-users',
        component: () => import('@/views/master/UsersView.vue')
      },
      {
        path: 'master/roles',
        name: 'master-roles',
        component: () => import('@/views/master/RolesView.vue')
      },
      {
        path: 'master/menus',
        name: 'master-menus',
        component: () => import('@/views/master/MenusView.vue')
      },
      {
        path: 'menus',
        redirect: '/master/menus'
      },
      {
        path: 'master/role-menus',
        name: 'master-role-menus',
        component: () => import('@/views/master/RoleMenusView.vue')
      },
      {
        path: 'role-menus',
        redirect: '/master/role-menus'
      },
      {
        path: 'master/data',
        name: 'master-data',
        component: () => import('@/views/master/MasterDataView.vue')
      },

      // 9. Audit & Notifikasi
      {
        path: 'audit-trail',
        name: 'audit-trail',
        component: () => import('@/views/audit/AuditTrailView.vue')
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('@/views/notifications/NotificationsView.vue')
      },

      // 10. Executive Support System (ESS)
      {
        path: 'ess/valuation-budget',
        name: 'ess-valuation-budget',
        component: () => import('@/views/ess/EssValuationBudgetView.vue')
      },
      {
        path: 'ess/cost-saving',
        name: 'ess-cost-saving',
        component: () => import('@/views/ess/EssCostSavingView.vue')
      },
      {
        path: 'ess/inventory-turnover',
        name: 'ess-inventory-turnover',
        component: () => import('@/views/ess/EssInventoryTurnoverView.vue')
      },
      {
        path: 'ess/risk-heatmap',
        name: 'ess-risk-heatmap',
        component: () => import('@/views/ess/EssRiskHeatmapView.vue')
      },
      {
        path: 'ess/service-level',
        name: 'ess-service-level',
        component: () => import('@/views/ess/EssServiceLevelView.vue')
      },
      {
        path: 'ess/audit-compliance',
        name: 'ess-audit-compliance',
        component: () => import('@/views/ess/EssAuditComplianceView.vue')
      },
      {
        path: 'ess/predictive-budget',
        name: 'ess-predictive-budget',
        component: () => import('@/views/ess/EssPredictiveBudgetView.vue')
      },

      // 11. Laporan & Rekapitulasi
      {
        path: 'reports/stock-valuation',
        name: 'reports-stock-valuation',
        component: () => import('@/views/reports/StockValuationReportView.vue')
      },
      {
        path: 'reports/stock-distribution',
        name: 'reports-stock-distribution',
        component: () => import('@/views/reports/StockDistributionReportView.vue')
      },
      {
        path: 'reports/procurement-coverage',
        name: 'reports-procurement-coverage',
        component: () => import('@/views/reports/ProcurementCoverageReportView.vue')
      },
      {
        path: 'reports/settlements',
        name: 'reports-settlements',
        component: () => import('@/views/reports/SettlementsReportView.vue')
      },
      {
        path: 'reports/general-ledger',
        name: 'reports-general-ledger',
        component: () => import('@/views/reports/GeneralLedgerReportView.vue')
      },
      {
        path: 'reports/ess',
        name: 'reports-ess',
        component: () => import('@/views/reports/EssReportsView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: (to) => ({
      name: 'access-denied',
      query: { from: to.fullPath, reason: 'unmatched' }
    })
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(createNavigationGuard({
  getAuthStore: () => useAuthStore(),
  getNavigationStore: () => useNavigationStore()
}));

export default router;
