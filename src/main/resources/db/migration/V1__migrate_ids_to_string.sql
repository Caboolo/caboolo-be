
ALTER TABLE hub MODIFY COLUMN hub_id VARCHAR(64) NOT NULL;

ALTER TABLE ride MODIFY COLUMN ride_id VARCHAR(64) NOT NULL;
ALTER TABLE ride MODIFY COLUMN source_hub_id VARCHAR(64) NOT NULL;
ALTER TABLE ride MODIFY COLUMN destination_hub_id VARCHAR(64) NOT NULL;

ALTER TABLE ride_user_mapping MODIFY COLUMN ride_user_mapping_id VARCHAR(64) NOT NULL;
ALTER TABLE ride_user_mapping MODIFY COLUMN ride_id VARCHAR(64) NOT NULL;

ALTER TABLE ride_user_request_mapping MODIFY COLUMN ride_user_request_mapping_id VARCHAR(64) NOT NULL;
ALTER TABLE ride_user_request_mapping MODIFY COLUMN ride_id VARCHAR(64) NOT NULL;
ALTER TABLE ride_user_request_mapping MODIFY COLUMN ride_user_mapping_id VARCHAR(64);

ALTER TABLE user_detail MODIFY COLUMN user_details_id VARCHAR(64) NOT NULL;

ALTER TABLE user_login MODIFY COLUMN user_login_id VARCHAR(64) NOT NULL;

ALTER TABLE waitlist_entry MODIFY COLUMN waitlist_entry_id VARCHAR(64) NOT NULL;

ALTER TABLE review MODIFY COLUMN review_id VARCHAR(64) NOT NULL;
ALTER TABLE review MODIFY COLUMN ride_id VARCHAR(64) NOT NULL;

ALTER TABLE flight_verification MODIFY COLUMN flight_verification_id VARCHAR(64) NOT NULL;

ALTER TABLE user_fcm_token MODIFY COLUMN user_fcm_token_id VARCHAR(64) NOT NULL;
