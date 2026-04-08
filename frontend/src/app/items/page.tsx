import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { ItemsBrowser } from "@/features/items/items-browser";

export const metadata = {
  title: "Itens | NexusMind",
  description: "Catálogo de itens do League of Legends com ícones do Data Dragon.",
};

export default function ItemsPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-6xl px-4 py-10">
        <h1 className="text-3xl font-semibold tracking-tight">Itens</h1>
        <p className="mt-2 max-w-2xl text-muted-foreground">
          Lista completa do patch atual, carregada do Data Dragon oficial (ícones e textos localizados em pt-BR quando
          disponível).
        </p>
        <div className="mt-8">
          <ItemsBrowser />
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
