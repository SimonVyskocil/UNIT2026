create table if not exists public.spot_swipes (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  spot_id bigint not null references public.spots(id) on delete cascade,
  action text not null check (action in ('liked', 'disliked')),
  created_at timestamp with time zone not null default now(),
  unique (user_id, spot_id)
);

alter table public.spot_swipes enable row level security;

create policy "users can read own swipes"
on public.spot_swipes
for select
using (auth.uid() = user_id);

create policy "users can insert own swipes"
on public.spot_swipes
for insert
with check (auth.uid() = user_id);

create policy "users can update own swipes"
on public.spot_swipes
for update
using (auth.uid() = user_id)
with check (auth.uid() = user_id);
