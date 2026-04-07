import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { DraftAnalyzerPanel } from "@/features/draft/draft-analyzer-panel";

export const metadata = {
  title: "Draft analyzer | NexusMind",
  description: "Relatório profissional de draft — comps, fases do jogo e win conditions.",
};

export default function ProfessionalPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-6xl px-4 py-10">
        <h1 className="text-3xl font-semibold tracking-tight">Modo profissional</h1>
        <p className="mt-2 max-w-2xl text-muted-foreground">
          Sidebar retrátil para navegar entre seções. Ideal para coaches e prep de scrim.
        </p>
        <div className="mt-8">
          <DraftAnalyzerPanel />
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
