Each phone covers the cases only it can test: iOS covers the subscription group and the StoreKit switches, Android covers parallel subscriptions and Play's test cards. Do them in order, because each step builds on the one before.

iPhone (from Xcode, local StoreKit)

Start clean: in Xcode, Debug → StoreKit → Manage Transactions → delete all, then delete HuKi from the phone and press Run again.

┌─────┬────────────────────────────────────────────────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────────────────────────────────────────┐
│  #  │                                               Do                                               │                                            Expect                                            │
├─────┼────────────────────────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────┤
│ 1   │ Buy Mókus                                                                                      │ It scrolls to the top, the card pops in with hearts and a haptic, and there's no chooser yet │
├─────┼────────────────────────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────┤
│ 2   │ Close Support during a purchase: tap Bagoly, then go back while the sheet is open, and confirm │ Reopen Support: Bagoly is recorded and the chooser shows 2 animals (fix 2)                   │
├─────┼────────────────────────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────┤
│ 3   │ Buy Mókus again                                                                                │ ×2 on Mókus, Mókus is chosen, the celebration plays                                          │
├─────┼────────────────────────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────┤
│ 4   │ Tap the animal that's already selected, then buy Sün                                           │ Sün still celebrates (fix 6)                                                                 │
├─────┼────────────────────────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────┤
│ 5   │ Switch animals in the chooser                                                                  │ The colours, icon, silhouette and text animate in place: no scroll, no hearts                │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 6   │ Buy Szarvas, then Medve                                                                        │ Only Medve shows "Aktív" (the switch within the subscription group)                          │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 7   │ Tap the "Aktív" Medve tier                                                                     │ The "Már megvásároltad" dialog opens                                                         │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 8   │ With 4 or more animals                                                                         │ The row scrolls sideways inside the panel                                                    │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 9   │ Kill and reopen the app                                                                        │ The choice and the counts are kept, and there's no celebration on opening (fix 6)            │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 10  │ Turn on Ask to Buy in HuKi.storekit, run again, buy Vaddisznó                                  │ The "Függőben lévő vásárlás" message appears                                                 │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 11  │ Approve it in Manage Transactions while on Support                                             │ The message disappears, then the celebration plays and Vaddisznó becomes the shown animal    │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 12  │ Same again, but put the app in the background before approving                                 │ Back in the app, it shows as purchased within a moment                                       │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 13  │ Turn on Purchase failure, then buy                                                             │ The "Sikertelen vásárlás" message appears                                                    │
├─────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 14  │ Turn on Load Products failure, then reopen Support                                             │ The prices-unavailable message appears, and Retry works once it's off again                  │
└─────┴─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

Turn all the switches off afterwards. Delete HuKi and reinstall: the one-time purchases come  history. They come back with no ×N counts, though, because HuKi kept those itself.

Pixel 8a (Play, license tester)

┌─────┬───────────────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────────────┐
│  #  │                                    Do                                     │                                        Expect                                        │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 15  │ Buy Sün with "Test card, always approves"                                 │ The celebration plays and the chooser grows                                          │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 16  │ Buy Bagoly again                                                          │ ×3                                                                                   │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 17  │ Close Support while the Play sheet is open, then finish the purchase      │ It's recorded when you reopen Support (fix 2)                                        │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 18  │ Buy Szarvas while Medve is active                                         │ Both show "Aktív" (Android allows parallel subscriptions)                            │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 19  │ "Slow test card": stay on Support                                         │ The pending message appears; after a few minutes it clears and the celebration plays │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 20  │ Slow test card, then open another tier's sheet while the first is pending │ That sheet's result is right, not wrongly "Pending" (fix 4)                          │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 21  │ Slow test card with the app in the background                             │ It shows as purchased when you return                                                │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 22  │ "Always declines" / close the sheet                                       │ The failed message / nothing                                                         │
├─────┼───────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
│ 23  │ Check the prices                                                          │ 499 Ft / 999 Ft / 1 499 Ft; in English "HUF 499"-style                               │
└─────┴───────────────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────────────┘

Both

- Dark mode, landscape, and English.
- Screen reader (VoiceOver / TalkBack): the chooser reads the name, whether it's selected, and the purchase count.
- "Előfizetések kezelése": on iOS it opens the Apple sheet; on Android it opens Play's subscr

For a real final check later: iOS Sandbox (my script plus a sandbox account) tests the actualwhich the local StoreKit file can't.