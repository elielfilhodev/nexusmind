/**
 * Textos do Data Dragon / cliente do LoL usam tags próprias
 * (`<rarityLegendary>`, `<mainText>`, `<br>`, `<silver>`…).
 * Converte para texto legível (sem renderizar HTML).
 */
export function sanitizeRiotPlainText(raw: string | null | undefined): string {
  if (raw == null || raw === "") return "";

  let s = String(raw);

  // Entidades HTML (várias passadas para casos como &amp;lt;tag&amp;gt;)
  for (let pass = 0; pass < 5; pass++) {
    const before = s;
    s = s
      .replace(/&nbsp;/gi, " ")
      .replace(/&#39;/g, "'")
      .replace(/&quot;/g, '"')
      .replace(/&lt;/gi, "<")
      .replace(/&gt;/gi, ">")
      .replace(/&amp;/g, "&");
    if (s === before) break;
  }

  // Remove tags (inclui nomes tipo subtitleLeft, rarityLegendary, mainText…)
  for (let i = 0; i < 10; i++) {
    const next = s.replace(/<\/?[a-zA-Z][a-zA-Z0-9_-]*[^>]*>/g, " ");
    if (next === s) break;
    s = next;
  }

  s = s.replace(/<\/?>/g, " ");

  return s.replace(/\s+/g, " ").trim();
}
