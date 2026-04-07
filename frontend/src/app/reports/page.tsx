import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { ReportsTable } from "@/features/reports/reports-table";

export const metadata = {
  title: "Relatórios | NexusMind",
  description: "Histórico de análises casual e draft.",
};

export default function ReportsPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-4xl px-4 py-10">
        <h1 className="text-3xl font-semibold tracking-tight">Relatórios recentes</h1>
        <p className="mt-2 text-muted-foreground">Paginação server-side pronta na API; MVP lista primeira página.</p>
        <div className="mt-8">
          <ReportsTable />
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
