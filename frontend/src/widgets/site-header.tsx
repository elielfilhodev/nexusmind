import Link from "next/link";
import { Sparkles } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const links = [
  { href: "/casual", label: "Solo Queue" },
  { href: "/professional", label: "Draft Pro" },
  { href: "/reports", label: "Relatórios" },
  { href: "/items", label: "Itens" },
  { href: "/runes", label: "Runas" },
];

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-50 border-b border-border/60 bg-background/80 backdrop-blur-md">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between gap-4 px-4">
        <Link href="/" className="flex items-center gap-2 font-semibold tracking-tight">
          <Sparkles className="size-5 text-primary" aria-hidden />
          <span>NexusMind</span>
        </Link>
        <nav className="hidden items-center gap-1 md:flex">
          {links.map((l) => (
            <Link
              key={l.href}
              href={l.href}
              className={cn(buttonVariants({ variant: "ghost", size: "sm" }))}
            >
              {l.label}
            </Link>
          ))}
        </nav>
        <Link
          href="/professional"
          className={cn(buttonVariants({ variant: "outline", size: "sm" }), "md:hidden")}
        >
          Menu
        </Link>
      </div>
    </header>
  );
}
