import { getClientSessionId } from "@/shared/lib/client-session";

const defaultBase = "http://localhost:8080";

export function getApiBase(): string {
  return process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? defaultBase;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly body?: string
  ) {
    super(message);
    this.name = "ApiError";
  }
}

function mergeHeaders(base: HeadersInit): HeadersInit {
  const sid = getClientSessionId();
  if (!sid) {
    return base;
  }
  return {
    ...base,
    "X-Client-Session-Id": sid,
  };
}

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${getApiBase()}${path}`, {
    headers: mergeHeaders({ Accept: "application/json" }),
    cache: "no-store",
  });
  if (!res.ok) {
    const text = await res.text();
    throw new ApiError(`GET ${path} falhou`, res.status, text);
  }
  return res.json() as Promise<T>;
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${getApiBase()}${path}`, {
    method: "POST",
    headers: mergeHeaders({
      Accept: "application/json",
      "Content-Type": "application/json",
    }),
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new ApiError(`POST ${path} falhou`, res.status, text);
  }
  return res.json() as Promise<T>;
}

/** PDF com o mesmo isolamento de sessão que GET /api/reports (cabeçalho obrigatório). */
export async function downloadReportPdf(id: string, kind: "CASUAL" | "DRAFT"): Promise<void> {
  const sid = getClientSessionId();
  if (!sid) {
    throw new ApiError("Sessão indisponível (abra no navegador).", 400);
  }
  const res = await fetch(`${getApiBase()}/api/reports/${encodeURIComponent(id)}/pdf?kind=${kind}`, {
    headers: { "X-Client-Session-Id": sid },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new ApiError(`PDF falhou`, res.status, text);
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `nexusmind-${kind}-${id}.pdf`;
  a.rel = "noreferrer";
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
