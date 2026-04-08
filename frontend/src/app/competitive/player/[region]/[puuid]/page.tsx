import { CompetitiveHeader } from "@/features/competitive/competitive-header";
import { PlayerProfileView } from "@/features/competitive/player-profile-view";
import { SiteFooter } from "@/widgets/site-footer";

export const metadata = {
  title: "Perfil do jogador",
  description: "Perfil competitivo com estatísticas, histórico e análises com IA.",
};

export default async function CompetitivePlayerPage({
  params,
}: {
  params: Promise<{ region: string; puuid: string }>;
}) {
  const { region, puuid: rawPuuid } = await params;
  const puuid = decodeURIComponent(rawPuuid);
  const reg = decodeURIComponent(region).toUpperCase();

  return (
    <>
      <CompetitiveHeader />
      <main className="mx-auto max-w-7xl px-4 py-8">
        <PlayerProfileView region={reg} puuid={puuid} />
      </main>
      <SiteFooter />
    </>
  );
}
