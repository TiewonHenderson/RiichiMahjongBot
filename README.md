# CantoMahjongBot
A Class Based mahjong bot that runs it's predictions based of a tile market system.

If a certain tile is considered more scarce then another tile, it will be considered more expensive than it.

A "price" will be assigned to each unique tile and the cheaper will be preferred to be used then expensive.

However the shape progress can overrides the tile market system:

As tile price increases, the shape progress impact decreases in proportion to tile price.

However if a shape is considered a two sided wait, the shape progress is extremely high, only a one of kind tile price can override this group.
