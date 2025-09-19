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
WHERE FTagId = 19 and `date` ='20250826'
ORDER BY sd.amount  DESC, utr.`date`  DESC;

-- 实时 tagid:5,6,7,17,19
SELECT
    sb.industry,
    CASE
        WHEN SUM(sd.amount) >= 100000000 THEN CONCAT(ROUND(SUM(sd.amount) / 100000000, 2), '亿')
        ELSE CONCAT(ROUND(SUM(sd.amount)/10000, 2), '万')
    END AS totalAmountWithUnit,
    GROUP_CONCAT(
        DISTINCT 
        CONCAT(
            sb.name,
            '(',
            CASE
                WHEN sd.amount >= 100000000 THEN CONCAT(ROUND(sd.amount / 100000000, 2), '亿')
                ELSE CONCAT(ROUND(sd.amount / 10000, 2), '万')
            END,
            "|",
            ROUND(((sd.close/sd.pre_close)-1)*100, 2), '%'
            ')'
        )
        ORDER BY sd.amount DESC
        SEPARATOR ','
    ) AS names,
    -- 计算比例值
    COUNT(*) / (SELECT COUNT(*) FROM stock_basic sb2 WHERE sb2.industry = sb.industry) AS ratio,
    COUNT(*) AS tagCount,
    -- 添加行业股票总数字段用于计算比例
    (SELECT COUNT(*) FROM stock_basic sb2 WHERE sb2.industry = sb.industry) AS industryStockCount
FROM user_tag_relation_realtime utr
    LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
    LEFT JOIN stock_realtime sd ON sb.ts_code = sd.ts_code
WHERE 1=1
    AND FTagId IN(5,6,7,17,19)
    AND sb.industry != '-'
    AND utr.symbol IN (
        SELECT symbol
        FROM user_tag_relation
        WHERE 1=1
            AND FTagId IN(5,6,7,17,19)
            AND date IN (
                SELECT date
                FROM (
                    SELECT DISTINCT date
                    FROM user_tag_relation 
                    WHERE date < CURDATE()
                    ORDER BY date DESC
                    LIMIT 9
                ) AS recent_dates
            )
        GROUP BY symbol
        HAVING COUNT(*) >= 1
    )
    AND utr.symbol IN (
        SELECT symbol
        FROM user_tag_relation_realtime
        WHERE FTagId = 801
    )
GROUP BY sb.industry
-- 修改排序方式：按 tagCount/行业股票数 的倒序排序
ORDER BY (COUNT(*) / (SELECT COUNT(*) FROM stock_basic sb2 WHERE sb2.industry = sb.industry)) DESC;



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
        round(sd.pct_chg,2) ,
        '%)'
        )
        ORDER BY sd.amount DESC
        SEPARATOR ','
        ) AS names,
        -- 计算比例值
        COUNT(*) / (SELECT COUNT(*) FROM stock_basic sb2 WHERE sb2.industry = sb.industry) AS ratio,
        COUNT(*) AS tagCount,
        -- 添加行业股票总数
        (SELECT COUNT(*) FROM stock_basic sb2 WHERE sb2.industry = sb.industry) AS industryStockCount
FROM user_tag_relation utr
    LEFT JOIN stock_basic sb ON utr.symbol = sb.symbol
    LEFT JOIN stock_daily sd ON sb.ts_code = sd.ts_code AND utr.date = sd.trade_date
WHERE 1=1
  AND FTagId IN(5,6,7,17,19)
  AND date = '20250918'
  AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE date IN (
    SELECT date
    FROM (
    SELECT DISTINCT date
    FROM user_tag_relation
    WHERE date < CURDATE()
    ORDER BY date DESC
    LIMIT 10
    ) AS recent_dates
    )
  AND FTagId IN(5,6,7,17,19)
    GROUP BY symbol
    HAVING COUNT(*) >= 1
    )
     AND utr.symbol IN (
    SELECT symbol
    FROM user_tag_relation
    WHERE date = '20250918'
    and FTagId = 801
    ) 
GROUP BY sb.industry
-- 修改排序方式：按 tagCount/行业股票数 的倒序排序
ORDER BY (COUNT(*) / (SELECT COUNT(*) FROM stock_basic sb2 WHERE sb2.industry = sb.industry)) DESC;


select *from daily_report dr order by trade_date desc;


select *from stock_realtime sr where name='海联讯' ;
select * from stock_daily sd where ts_code ='002530.SZ' and trade_date ='20250828';
select * from user_tag_relation_realtime utrr where symbol ='002878';
select * from stock_basic sb where ts_code ='300277.SZ';
select  * from user_tag_relation utr where symbol ='002530' order by `date` desc;
select * from stock_daily sd order by trade_date desc;
delete from stock_daily where trade_date ='20250919';
delete from user_tag_relation where `date` ='20250919';
delete from daily_report where trade_date ='20250919';
select count(1) from stock_basic sb where industry ='家电行业';


