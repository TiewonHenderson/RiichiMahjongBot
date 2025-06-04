RiichiMahjongBot
A Riichi mahjong bot written completely from scratch that uses consecutive discards by opponents to identify which yaku (type of hand) they are going for. Once a confidence threshold is reached, the information is sent to the algorithm to label tiles within a Tile Map to display more dangerous tiles over safer tiles.

For example, if the algorithm determines an opponent discards bamboo tiles extremely early, bamboo tiles around the discard are labelled safer.