import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { RunesBrowser } from "@/features/runes/runes-browser";

export const metadata = {
  title: "Runas | NexusMind",
  description: "Árvores de runas do patch atual com ícones do Data Dragon.",
};

export default function RunesPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-6xl px-4 py-10">
        <h1 className="text-3xl font-semibold tracking-tight">Runas</h1>
        <p className="mt-2 max-w-2xl text-muted-foreground">
          Todas as árvores e runas do patch atual, carregadas do Data Dragon oficial (textos em pt-BR quando disponível).
        </p>
        <div className="mt-8">
          <RunesBrowser />
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
