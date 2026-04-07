import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { CasualAnalysisPanel } from "@/features/casual/casual-analysis-panel";

export const metadata = {
  title: "Solo queue | NexusMind",
  description: "Análise para subir de elo — picks, bans, runas e macro por lane.",
};

export default function CasualPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-6xl px-4 py-10">
        <h1 className="text-3xl font-semibold tracking-tight">Modo casual / solo queue</h1>
        <p className="mt-2 max-w-2xl text-muted-foreground">
          A resposta é JSON estruturado gerado pela IA (ou fallback heurístico) e persistida para histórico e PDF.
        </p>
        <div className="mt-8">
          <CasualAnalysisPanel />
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
