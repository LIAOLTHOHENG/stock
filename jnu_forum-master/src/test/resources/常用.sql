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
WHERE FTagId = 17 and `date` ='20250731'
ORDER BY sd.amount  DESC, utr.`date`  DESC;


--某个标签类型下的行业分布

SELECT
    sb.industry,
    CASE
        WHEN SUM(sd.amount) >= 100000 THEN CONCAT(ROUND(SUM(sd.amount) / 100000, 2), '亿')
        ELSE CONCAT(ROUND(SUM(sd.amount)/10, 2), '万')
        END AS totalAmountWithUnit,
    GROUP_CONCAT(
            CONCAT(
                    sb.name,
                    '(',
                    CASE
                        WHEN sd.amount >= 100000 THEN CONCAT(ROUND(sd.amount / 100000, 2), '亿')
                        ELSE CONCAT(ROUND(sd.amount / 10, 2), '万')
                        END,
                    ')'
            )
                ORDER BY sd.amount DESC
        SEPARATOR ','
    ) AS names,
    COUNT(*) AS tagCount
FROM user_tag_relation utr
    LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
    LEFT JOIN stock_daily sd ON sb.ts_code = sd.ts_code AND utr.`date` = sd.trade_date
WHERE FTagId = 17 AND `date` = '20250730'
GROUP BY sb.industry
ORDER BY tagCount DESC;

--实时标签
SELECT
    sb.industry,
    CASE
        WHEN SUM(sd.amount) >= 100000000 THEN CONCAT(ROUND(SUM(sd.amount) / 100000000, 2), '亿')
        ELSE CONCAT(ROUND(SUM(sd.amount)/10000, 2), '万')
        END AS totalAmountWithUnit,
    GROUP_CONCAT(
            CONCAT(
                    sb.name,
                    '(',
                    CASE
                        WHEN sd.amount >= 100000000 THEN CONCAT(ROUND(sd.amount / 100000000, 2), '亿')
                        ELSE CONCAT(ROUND(sd.amount / 10000, 2), '万')
                        END,
                    ')'
            )
                ORDER BY sd.amount DESC
        SEPARATOR ','
    ) AS names,
    COUNT(*) AS tagCount
FROM user_tag_relation_realtime utr
    LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
    LEFT JOIN stock_realtime  sd ON sb.ts_code = sd.ts_code
WHERE FTagId = 17 and sb.industry !='-'
GROUP BY sb.industry
ORDER BY tagCount DESC;


--实施标签 严苛版
SELECT
    sb.industry,
    CASE
        WHEN SUM(sd.amount) >= 100000000 THEN CONCAT(ROUND(SUM(sd.amount) / 100000000, 2), '亿')
        ELSE CONCAT(ROUND(SUM(sd.amount)/10000, 2), '万')
        END AS totalAmountWithUnit,
    GROUP_CONCAT(
            CONCAT(
                    sb.name,
                    '(',
                    CASE
                        WHEN sd.amount >= 100000000 THEN CONCAT(ROUND(sd.amount / 100000000, 2), '亿')
                        ELSE CONCAT(ROUND(sd.amount / 10000, 2), '万')
                        END,
                    ')'
            )
                ORDER BY sd.amount DESC
        SEPARATOR ','
    ) AS names,
        COUNT(*) AS tagCount
FROM user_tag_relation_realtime utr
    LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
    LEFT JOIN stock_realtime sd ON sb.ts_code = sd.ts_code
WHERE 1=1
  AND FTagId IN(5,6,17)
  AND sb.industry != '-'
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE 1=1
  AND FTagId IN(5,6,17)
  AND date IN (
    SELECT date
    FROM (
    SELECT DISTINCT date
    FROM user_tag_relation
    ORDER BY date DESC
    LIMIT 10
    ) AS recent_dates
    )
    GROUP BY symbol
    HAVING COUNT(*) >= 3
    )
GROUP BY sb.industry
ORDER BY tagCount DESC;

--盘后 平稳条件
SELECT
    sb.industry,
    CASE
        WHEN SUM(sd.amount) >= 100000 THEN CONCAT(ROUND(SUM(sd.amount) / 100000, 2), '亿')
        ELSE CONCAT(ROUND(SUM(sd.amount)/10, 2), '万')
        END AS totalAmountWithUnit,
    GROUP_CONCAT(
        distinct
        CONCAT(
        sb.name,
        '(',
        CASE
        WHEN sd.amount >= 100000 THEN CONCAT(ROUND(sd.amount / 100000, 2), '亿')
        ELSE CONCAT(ROUND(sd.amount / 10, 2), '万')
        END,
        ')'
        )
        ORDER BY sd.amount DESC
        SEPARATOR ','
        ) AS names,
        COUNT(*) AS tagCount
FROM user_tag_relation utr
    LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
    LEFT JOIN stock_daily sd ON sb.ts_code = sd.ts_code AND utr.date = sd.trade_date
WHERE 1=1
  AND FTagId IN(5,6,7,17)
  AND date = '2025-08-13'
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE date IN (
    SELECT date
    FROM (
    SELECT DISTINCT date
    FROM user_tag_relation
    ORDER BY date DESC
    limit 6
    ) AS recent_dates
    )
  AND FTagId IN(5,6,7,17)
    GROUP BY symbol
    HAVING COUNT(*) >= 2
    )
-- 新增条件：要求symbol在今天有FTagId=801的记录
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE date = '2025-08-13'
  AND FTagId = 801
    )
GROUP BY sb.industry
ORDER BY tagCount desc;



