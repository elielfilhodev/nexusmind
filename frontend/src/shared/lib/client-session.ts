const STORAGE_KEY = "nexusmind_client_session";

/**
 * Identificador estável por navegador (localStorage), para o backend isolar relatórios.
 * Retorna null fora do cliente (SSR).
 */
export function getClientSessionId(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    let id = localStorage.getItem(STORAGE_KEY);
    if (!id || id.length < 8) {
      id = crypto.randomUUID();
      localStorage.setItem(STORAGE_KEY, id);
    }
    return id;
  } catch {
    return null;
  }
}
