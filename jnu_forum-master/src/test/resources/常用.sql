-- 孤独星球
SELECT
    utr.description,
    utr.symbol,
    sb.name,
    sb.industry,
    CASE
        WHEN sb.totalMarketCap >= 100000000 THEN CONCAT(ROUND(sb.totalMarketCap / 100000000, 2), '亿')
        ELSE CONCAT(ROUND(sb.totalMarketCap / 10000, 2), '万')
        END AS totalMarketCapWithUnit,
    CASE

        WHEN sd.amount >= 100000 THEN CONCAT(ROUND(sd.amount / 100000, 2), '亿')
        ELSE CONCAT(ROUND(sd.amount / 10, 2), '万')
        END AS amountWithUnit,
    utr.`date`
FROM user_tag_relation utr
         LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
         LEFT JOIN stock_daily sd ON sb.ts_code = sd.ts_code AND utr.`date`  = sd.trade_date
WHERE FTagId = 17 and `date` ='20250725'
ORDER BY sb.totalMarketCap DESC, utr.`date`  DESC;
