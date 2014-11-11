alter table festival drop column video_embed_code;

-- remove festival comments
DELETE FROM comment_link WHERE TYPE = 'festival';
DELETE FROM comment WHERE id NOT IN (SELECT DISTINCT comment_id FROM comment_link);

UPDATE performance SET priority = 'midline' WHERE priority = 'lowline';