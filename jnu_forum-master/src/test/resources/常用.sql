-- 实时 tagid:17
SELECT sb.industry,
       CASE
           WHEN SUM(sd.amount) >= 100000000 THEN CONCAT(ROUND(SUM(sd.amount) / 100000000, 2), '亿')
           ELSE CONCAT(ROUND(SUM(sd.amount) / 10000, 2), '万')
           END AS totalAmountWithUnit,
       GROUP_CONCAT(
           distinct
        CONCAT(
        sb.name,
        '(',
        CASE
        WHEN sd.amount >= 100000000 THEN CONCAT(ROUND(sd.amount / 100000000, 2), '亿')
        ELSE CONCAT(ROUND(sd.amount / 10000, 2), '万')
        END,
        ')'
        ) ORDER BY sd.amount DESC
        SEPARATOR ','
        ) AS names,
        COUNT(*) AS tagCount
FROM user_tag_relation_realtime utr
    LEFT JOIN stock_basic sb
ON utr.symbol = sb.symbol
    LEFT JOIN stock_realtime sd ON sb.ts_code = sd.ts_code
WHERE 1=1
  AND FTagId in(17)
  and sb.industry !='-'
GROUP BY sb.industry
ORDER BY tagCount DESC;

-- 实时 tagid:5,6,7,17
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
                    "|",
                    round(((sd.close/sd.pre_close)-1)*100,2) ,'%'
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
  AND FTagId IN(5,6,7,17)
  AND sb.industry != '-'
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE 1=1
  AND FTagId IN(5,6,7,17)
  AND date IN (
    SELECT date
    FROM (
    SELECT DISTINCT date
    FROM user_tag_relation
    ORDER BY date DESC
    LIMIT 5
    ) AS recent_dates
    )
    GROUP BY symbol
    HAVING COUNT(*) >= 2
    )
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation_realtime
    WHERE FTagId = 801
    )
GROUP BY sb.industry
ORDER BY tagCount DESC;



-- 盘后
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
        "|",
        sd.pct_chg ,
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
  AND date = '20250815'
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE date IN (
    SELECT date
    FROM (
    SELECT DISTINCT date
    FROM user_tag_relation
    ORDER BY date DESC
    LIMIT 5
    ) AS recent_dates
    )
  AND FTagId IN(5,6,7,17)
    GROUP BY symbol
    HAVING COUNT(*) >= 2+1
    )
GROUP BY sb.industry
ORDER BY tagCount desc;