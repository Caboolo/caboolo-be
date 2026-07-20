-- Insert Help Topics
INSERT INTO help_topic (id, created_at, updated_at, is_deleted, help_topic_id, title, description, display_order)
VALUES 
(1, NOW(), NOW(), false, 'HT-001', 'Getting Started & Account', 'Learn how to verify your profile and manage your account.', 1),
(2, NOW(), NOW(), false, 'HT-002', 'Creating & Joining Rides', 'Understand how to create, join, and manage your rides.', 2),
(3, NOW(), NOW(), false, 'HT-003', 'Flight Verification', 'Details about the Verified Traveler badge and process.', 3),
(4, NOW(), NOW(), false, 'HT-004', 'Safety & Trust', 'Learn about our safety features and how to report issues.', 4);

-- Insert Help Articles for "Getting Started & Account" (HT-001)
INSERT INTO help_article (id, created_at, updated_at, is_deleted, help_article_id, help_topic_id, question, answer, display_order)
VALUES 
(1, NOW(), NOW(), false, 'HA-001', 'HT-001', 'How do I create an account and verify my profile?', 'To create an account, sign up using your email or phone number. We will send a verification code to authenticate your profile.', 1),
(2, NOW(), NOW(), false, 'HA-002', 'HT-001', 'How does the rating and review system work?', 'After a ride, you can rate and review your co-passengers. This helps build a trusted community. Consistent positive ratings will improve your profile standing.', 2);

-- Insert Help Articles for "Creating & Joining Rides" (HT-002)
INSERT INTO help_article (id, created_at, updated_at, is_deleted, help_article_id, help_topic_id, question, answer, display_order)
VALUES 
(3, NOW(), NOW(), false, 'HA-003', 'HT-002', 'How do I create/schedule a new ride?', 'Go to the "Create Ride" section, select your source and destination hubs (like an airport), choose your departure time, and specify available seats.', 1),
(4, NOW(), NOW(), false, 'HA-004', 'HT-002', 'What are Hubs?', 'Hubs are designated, safe meeting points, such as specific airport terminals or tech parks, where all rides must start or end.', 2),
(5, NOW(), NOW(), false, 'HA-005', 'HT-002', 'How do Women-Only rides work?', 'When creating a ride, female users can toggle the "Women Only" option. These rides will only be visible to, and can only be joined by, other female users.', 3);

-- Insert Help Articles for "Flight Verification" (HT-003)
INSERT INTO help_article (id, created_at, updated_at, is_deleted, help_article_id, help_topic_id, question, answer, display_order)
VALUES 
(6, NOW(), NOW(), false, 'HA-006', 'HT-003', 'What is a Verified Traveler?', 'A Verified Traveler is someone who has provided a valid flight number and date. This badge adds an extra layer of trust.', 1),
(7, NOW(), NOW(), false, 'HA-007', 'HT-003', 'How long does my verification last?', 'Your verification is valid from 12 hours before your flight departs until 12 hours after it arrives.', 2);

-- Insert Help Articles for "Safety & Trust" (HT-004)
INSERT INTO help_article (id, created_at, updated_at, is_deleted, help_article_id, help_topic_id, question, answer, display_order)
VALUES 
(8, NOW(), NOW(), false, 'HA-008', 'HT-004', 'Is it safe to ride with strangers?', 'We prioritize safety through Flight Verification, User Ratings, Women-Only rides, and Hub-to-Hub tracking. Always meet at the designated Hub.', 1),
(9, NOW(), NOW(), false, 'HA-009', 'HT-004', 'How do I report a user?', 'If you experience any issues, you can report a user directly from their profile or the ride details page. Our support team will investigate promptly.', 2);
