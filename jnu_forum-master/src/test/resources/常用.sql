-- 孤独星球
select utr .description ,sb.name,utr .`date`  from user_tag_relation utr left join stock_basic sb  on utr.symbol =sb.symbol  where FTagId =17 order by `date` desc;
