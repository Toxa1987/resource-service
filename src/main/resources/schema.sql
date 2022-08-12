DROP TABLE  IF EXISTS `saved_songs` ;
CREATE TABLE `saved_songs` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `song_file_name` varchar(50) NOT NULL,
                         `location` varchar(250) NOT NULL,
                         `storage_id` bigint(20) NOT NULL,
                          PRIMARY KEY (`id`));