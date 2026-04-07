"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

function isPlainObject(v: unknown): v is Record<string, unknown> {
  return typeof v === "object" && v !== null && !Array.isArray(v);
}

export function StructuredView({ data, title }: { data: unknown; title?: string }) {
  if (data === null || data === undefined) {
    return null;
  }
  if (Array.isArray(data)) {
    return (
      <ul className="list-inside list-disc space-y-1 text-sm text-muted-foreground">
        {data.map((item, i) => (
          <li key={i}>
            {isPlainObject(item) || Array.isArray(item) ? (
              <StructuredView data={item} />
            ) : (
              String(item)
            )}
          </li>
        ))}
      </ul>
    );
  }
  if (isPlainObject(data)) {
    const entries = Object.entries(data);
    return (
      <div className="space-y-4">
        {title ? (
          <h3 className="text-lg font-semibold tracking-tight">{title}</h3>
        ) : null}
        <div className="grid gap-3 sm:grid-cols-1">
          {entries.map(([key, value]) => (
            <Card key={key} className="border-border/80 bg-card/50">
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center gap-2 text-base font-medium capitalize">
                  {key.replace(/([A-Z])/g, " $1").trim()}
                  {typeof value === "string" && value.length < 40 ? (
                    <Badge variant="secondary" className="font-normal">
                      texto
                    </Badge>
                  ) : null}
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm">
                {typeof value === "string" || typeof value === "number" || typeof value === "boolean" ? (
                  <p className="leading-relaxed text-foreground">{String(value)}</p>
                ) : (
                  <StructuredView data={value} />
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }
  return <span className="text-sm">{String(data)}</span>;
}
