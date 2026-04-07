import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { ReportDetail } from "@/features/reports/report-detail";

type Props = {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ kind?: string }>;
};

export async function generateMetadata({ params, searchParams }: Props) {
  const { id } = await params;
  const sp = await searchParams;
  return {
    title: `Relatório ${id.slice(0, 8)}… | NexusMind`,
    description: `Detalhe ${sp.kind ?? "CASUAL"}`,
  };
}

export default async function ReportPage({ params, searchParams }: Props) {
  const { id } = await params;
  const sp = await searchParams;
  const kind = sp.kind === "DRAFT" ? "DRAFT" : "CASUAL";

  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-5xl px-4 py-10">
        <ReportDetail id={id} kind={kind} />
      </main>
      <SiteFooter />
    </>
  );
}
