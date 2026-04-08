import { CompetitiveHeader } from "@/features/competitive/competitive-header";
import { LeaderboardView } from "@/features/competitive/leaderboard-view";
import { SiteFooter } from "@/widgets/site-footer";

export const metadata = {
  title: "Leaderboard competitivo",
  description: "Ranking regional, busca de jogadores e integração com a Riot API.",
};

export default function CompetitiveLeaderboardPage() {
  return (
    <>
      <CompetitiveHeader />
      <main className="mx-auto max-w-7xl px-4 py-8">
        <LeaderboardView />
      </main>
      <SiteFooter />
    </>
  );
}
