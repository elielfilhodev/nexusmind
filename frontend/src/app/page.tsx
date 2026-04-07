import Link from "next/link";
import { ArrowRight, BarChart3, Shield } from "lucide-react";
import { SiteHeader } from "@/widgets/site-header";
import { SiteFooter } from "@/widgets/site-footer";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export default function HomePage() {
  return (
    <>
      <SiteHeader />
      <main>
        <section className="mx-auto max-w-6xl px-4 py-16 md:py-24">
          <div className="max-w-3xl">
            <p className="text-sm font-medium uppercase tracking-widest text-primary">League of Legends</p>
            <h1 className="mt-3 text-4xl font-semibold tracking-tight md:text-5xl lg:text-6xl">
              Análise estratégica com IA — do solo queue ao draft profissional
            </h1>
            <p className="mt-6 text-lg text-muted-foreground leading-relaxed">
              NexusMind combina dados versionados por patch, heurísticas de composição e um provedor de IA plugável
              para entregar relatórios acionáveis, exportáveis em PDF, sem necessidade de login nesta versão.
            </p>
            <div className="mt-10 flex flex-wrap gap-3">
              <Link
                href="/casual"
                className={cn(buttonVariants({ size: "lg" }), "inline-flex items-center")}
              >
                Modo solo queue
                <ArrowRight className="ml-2 size-4" />
              </Link>
              <Link href="/professional" className={cn(buttonVariants({ size: "lg", variant: "outline" }))}>
                Draft analyzer
              </Link>
            </div>
          </div>
        </section>

        <section className="border-y border-border/60 bg-muted/20 py-16">
          <div className="mx-auto grid max-w-6xl gap-6 px-4 md:grid-cols-2">
            <Card className="border-border/80 bg-card/80">
              <CardHeader>
                <Shield className="mb-2 size-8 text-primary" />
                <CardTitle>Solo queue / climb</CardTitle>
                <CardDescription>
                  Picks, bans, runas, spells, builds, plano de lane, erros comuns do elo e roteiro para subir de rank.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Link href="/casual" className={cn(buttonVariants({ variant: "secondary" }))}>
                  Abrir análise casual
                </Link>
              </CardContent>
            </Card>
            <Card className="border-border/80 bg-card/80">
              <CardHeader>
                <BarChart3 className="mb-2 size-8 text-primary" />
                <CardTitle>Draft profissional</CardTitle>
                <CardDescription>
                  Comps, matchups por rota, pathing, win/lose conditions, objetivos e plano minuto a minuto por fase.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Link href="/professional" className={cn(buttonVariants({ variant: "secondary" }))}>
                  Abrir draft analyzer
                </Link>
              </CardContent>
            </Card>
          </div>
        </section>

        <section className="mx-auto max-w-6xl px-4 py-16">
          <h2 className="text-2xl font-semibold tracking-tight">Por que NexusMind</h2>
          <ul className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3 text-sm text-muted-foreground">
            <li className="rounded-lg border border-border/60 p-4">
              <strong className="text-foreground">Arquitetura limpa</strong> — backend Java/Spring em camadas, frontend modular,
              IA abstraída para trocar provedor sem reescrever domínio.
            </li>
            <li className="rounded-lg border border-border/60 p-4">
              <strong className="text-foreground">Segurança</strong> — validação, rate limit, CORS explícito, headers HTTP e
              payloads limitados; pronto para auth futura.
            </li>
            <li className="rounded-lg border border-border/60 p-4">
              <strong className="text-foreground">Dados evolutivos</strong> — seed Flyway hoje; porta Data Dragon / stats
              amanhã, sem quebrar contratos da API.
            </li>
          </ul>
        </section>
      </main>
      <SiteFooter />
    </>
  );
}
