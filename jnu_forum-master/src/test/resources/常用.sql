-- 孤独星球
select utr .description ,utr .symbol ,sb.name,sb.industry ,utr .`date`  from user_tag_relation utr left join stock_basic sb  on utr.symbol =sb.symbol  where FTagId =17 order by `date` desc;

