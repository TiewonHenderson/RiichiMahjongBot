# CantoMahjongBot
A sophisticated Mahjong bot utilizing a class-based system with predictive capabilities grounded in a dynamic tile market. 

Each unique tile is assigned a 'price' based on its perceived scarcity, with cheaper tiles being prioritized over more expensive ones during gameplay.

However, the bot incorporates a nuanced approach where the influence of shape progress can override the tile market system. 

As the price of a tile increases, the impact of shape progress decreases proportionally.

On the other hand, if a shape is identified as a good shape (i,e two-sided wait), the shape progress holds significant importance, 

This can only be overrided by an extreme scarce (i.e one-of-a-kind tile = only 1 tile is "available", other in drop pile) tile acceptance.
