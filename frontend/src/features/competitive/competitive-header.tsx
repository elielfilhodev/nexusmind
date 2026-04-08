"use client";

import Link from "next/link";
import { Crown, LayoutGrid } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function CompetitiveHeader() {
  return (
    <header className="sticky top-0 z-50 border-b border-border/60 bg-background/85 backdrop-blur-xl">
      <div className="mx-auto flex h-14 max-w-7xl items-center justify-between gap-4 px-4">
        <div className="flex items-center gap-3">
          <Link href="/" className={cn(buttonVariants({ variant: "ghost", size: "sm" }), "text-muted-foreground")}>
            ← Início
          </Link>
          <div className="hidden h-4 w-px bg-border sm:block" />
          <Link href="/competitive/leaderboard" className="flex items-center gap-2 font-semibold tracking-tight">
            <Crown className="size-5 text-amber-400" aria-hidden />
            <span>Competitivo</span>
            <span className="hidden rounded-full border border-amber-500/40 bg-amber-500/10 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wider text-amber-200 sm:inline">
              Premium
            </span>
          </Link>
        </div>
        <nav className="flex items-center gap-1">
          <Link
            href="/competitive/leaderboard"
            className={cn(buttonVariants({ variant: "secondary", size: "sm" }), "gap-1.5")}
          >
            <LayoutGrid className="size-4" />
            Leaderboard
          </Link>
        </nav>
      </div>
    </header>
  );
}
