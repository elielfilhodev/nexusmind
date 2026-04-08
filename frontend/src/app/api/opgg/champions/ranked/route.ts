import { NextResponse } from "next/server";

/** Cada chamada busca dados atualizados na OP.GG (sem cache de rota). */
export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const res = await fetch("https://lol-api-champion.op.gg/api/global/champions/ranked", {
      headers: { Accept: "application/json" },
      cache: "no-store",
    });
    if (!res.ok) {
      return NextResponse.json(
        { error: "opgg_upstream", status: res.status },
        { status: 502 }
      );
    }
    const json = (await res.json()) as unknown;
    return NextResponse.json(json);
  } catch {
    return NextResponse.json({ error: "fetch_failed" }, { status: 502 });
  }
}
