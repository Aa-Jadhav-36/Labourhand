-- ============================================================
-- LabourHand Seed Data
-- Passwords are BCrypt hashes of: worker123 / owner123
-- BCrypt of "worker123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjTa6T8AfVC
-- BCrypt of "owner123":  $2a$10$8K1p/a0dR1xqM4aaRobKReAowQs.C72K95K6FHyZn2S2M7yDMsJxi
-- ============================================================

-- Skills
INSERT IGNORE INTO skills (name) VALUES
  ('Masonry'), ('Painting'), ('Plastering'), ('Electrical'),
  ('Plumbing'), ('Carpentry'), ('Tiling'), ('Concrete Work'),
  ('Waterproofing'), ('Stone Masonry'), ('Interior Design'), ('Texture Work');

-- ============================================================
-- USERS – Workers
-- ============================================================
INSERT IGNORE INTO users (name, email, phone, password_hash, role, avatar, verified, language, created_at) VALUES
  ('Rajesh Kumar',  'rajesh@labourhand.com', '+91 9876543210',
   '$2a$10$mf28BKyvxe.wXkPbylHewOeGB6i4k1FRE6KXIlHiKcYA7LTXd0eRy',
   'WORKER',
   'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop',
   true, 'en', NOW()),

  ('Amit Patel',    'amit@labourhand.com',   '+91 9876543211',
   '$2a$10$mf28BKyvxe.wXkPbylHewOeGB6i4k1FRE6KXIlHiKcYA7LTXd0eRy',
   'WORKER',
   'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop',
   true, 'en', NOW()),

  ('Suresh Singh',  'suresh@labourhand.com', '+91 9876543212',
   '$2a$10$mf28BKyvxe.wXkPbylHewOeGB6i4k1FRE6KXIlHiKcYA7LTXd0eRy',
   'WORKER',
   'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop',
   true, 'en', NOW()),

  ('Vijay Kumar',   'vijay@labourhand.com',  '+91 9876543213',
   '$2a$10$mf28BKyvxe.wXkPbylHewOeGB6i4k1FRE6KXIlHiKcYA7LTXd0eRy',
   'WORKER',
   'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop',
   false, 'en', NOW()),

  ('Ravi Sharma',   'ravi@labourhand.com',   '+91 9876543214',
   '$2a$10$mf28BKyvxe.wXkPbylHewOeGB6i4k1FRE6KXIlHiKcYA7LTXd0eRy',
   'WORKER',
   'https://images.unsplash.com/photo-1519345182560-3f2917c472ef?w=200&h=200&fit=crop',
   false, 'en', NOW());

-- ============================================================
-- USERS – Owners
-- ============================================================
INSERT IGNORE INTO users (name, email, phone, password_hash, role, avatar, verified, language, created_at) VALUES
  ('Priya Sharma',  'priya@labourhand.com',  '+91 9876543215',
   '$2a$10$yOiLsm0BT8vsqzS7m4xFp.yrvxRFrIcFmd.HwqiFvOEGi6lsN8eoi',
   'OWNER',
   'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop',
   true, 'en', NOW()),

  ('Arjun Mehta',   'arjun@labourhand.com',  '+91 9876543216',
   '$2a$10$yOiLsm0BT8vsqzS7m4xFp.yrvxRFrIcFmd.HwqiFvOEGi6lsN8eoi',
   'OWNER',
   'https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=200&h=200&fit=crop',
   true, 'en', NOW());

-- ============================================================
-- WORKER PROFILES (user_id matches users table auto-increment: 1-5)
-- ============================================================
INSERT IGNORE INTO worker_profiles (user_id, specialization, years_experience, rating, completed_jobs, bio, skills_india_verified, on_time_rate, rehire_rate, status) VALUES
  (1, 'Senior Mason',          15, 4.8, 127,
   'Experienced mason specializing in brick-laying, tiling, and concrete work.',
   true, 98.0, 95.0, 'available'),

  (2, 'Professional Painter',  8,  4.6,  89,
   'Expert painter with Asian Paints certification. Quality guaranteed.',
   true, 92.0, 88.0, 'available'),

  (3, 'Master Electrician',    12, 4.9, 156,
   'Licensed electrician specializing in residential and commercial wiring.',
   true, 99.0, 97.0, 'available'),

  (4, 'Senior Plumber',        10, 4.5,  76,
   'Experienced plumber with expertise in pipe fitting and sanitation.',
   false, 90.0, 85.0, 'available'),

  (5, 'Expert Carpenter',       6, 4.7,  54,
   'Skilled carpenter specializing in furniture and interior woodwork.',
   false, 94.0, 90.0, 'available');

-- Worker Skills (skill IDs: 1=Masonry,2=Painting,3=Plastering,4=Electrical,5=Plumbing,6=Carpentry,7=Tiling,8=Concrete Work,9=Waterproofing)
INSERT IGNORE INTO worker_skills (worker_id, skill_id) VALUES
  (1,1),(1,3),(1,7),(1,8),(1,10),  -- Rajesh: Mason
  (2,2),(2,3),(2,9),                -- Amit: Painter
  (3,4),                            -- Suresh: Electrician
  (4,5),(4,9),                      -- Vijay: Plumber
  (5,6),(5,7);                      -- Ravi: Carpenter

-- Certifications
INSERT IGNORE INTO certifications (worker_id, name, issuer, year) VALUES
  (1, 'Skills India Certified Mason',       'National Skill Development Corporation', '2023'),
  (1, 'Construction Safety Training',       'CIDC',                                   '2024'),
  (2, 'Asian Paints Certified Applicator',  'Asian Paints Ltd.',                      '2022'),
  (3, 'Licensed Electrical Contractor',     'BESCOM',                                 '2021'),
  (3, 'Construction Safety Training',       'CIDC',                                   '2023');

-- ============================================================
-- OWNER PROFILES (user_id: 6=Priya, 7=Arjun)
-- ============================================================
INSERT IGNORE INTO owner_profiles (user_id, company_name, projects_posted) VALUES
  (6, 'Sharma Constructions', 3),
  (7, 'Mehta Builders',       2);

-- ============================================================
-- PROJECTS (owner_id 6 = Priya, 7 = Arjun)
-- ============================================================
INSERT IGNORE INTO projects (title, description, category, budget, timeline_days, location, lat, lng, status, owner_id, progress, created_at) VALUES
  ('Apartment Painting',
   'Need experienced painters for a 3BHK apartment. Walls and ceiling work required. High-quality finish expected.',
   'Painting', 10000, 5, 'Indiranagar, Bangalore', 12.9716, 77.6412, 'OPEN_FOR_BIDS', 6, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),

  ('Kitchen Renovation',
   'Full kitchen renovation including tiling, cabinet fitting, and plumbing. Modern design preferred.',
   'Masonry', 45000, 7, 'Koramangala, Bangalore', 12.9352, 77.6245, 'IN_PROGRESS', 6, 45, DATE_SUB(NOW(), INTERVAL 3 DAY)),

  ('Electrical Wiring - Office',
   'Complete electrical rewiring for 1500 sqft office space. Includes panel upgrade and safety certification.',
   'Electrical', 15000, 3, 'Whitefield, Bangalore', 12.9698, 77.7499, 'OPEN_FOR_BIDS', 7, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),

  ('Villa Construction - Foundation',
   'Foundation and basement work for 4BHK villa. Heavy masonry and concrete work.',
   'Construction', 200000, 45, 'HSR Layout, Bangalore', 12.9122, 77.6446, 'COMPLETED', 7, 100, DATE_SUB(NOW(), INTERVAL 60 DAY)),

  ('Bathroom Tiling',
   'Tiling work for 2 bathrooms. Premium tiles to be used. Clean finish required.',
   'Masonry', 8500, 3, 'BTM Layout, Bangalore', 12.9121, 77.6085, 'PAYMENT_VERIFIED', 6, 100, DATE_SUB(NOW(), INTERVAL 14 DAY));

-- ============================================================
-- BIDS (project 1 = Apartment Painting, project 2 = Kitchen)
-- ============================================================
INSERT IGNORE INTO bids (project_id, worker_id, amount, estimated_days, message, status, recommended, submitted_at) VALUES
  -- Project 1: Apartment Painting (open for bids)
  (1, 1, 9500,  4, 'I have 15 years of experience in painting. Can provide samples. Free touch-up for 1 year.', 'PENDING', true,  DATE_SUB(NOW(), INTERVAL 2 HOUR)),
  (1, 2, 8800,  5, 'Professional painter with Asian Paints certification. Free color consultation included.',   'PENDING', false, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
  (1, 4, 11200, 3, 'Premium painting service with designer finishes. Eco-friendly paints used.',               'PENDING', false, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (1, 5, 7500,  6, 'Budget-friendly painting service. Quality guaranteed. References available.',              'PENDING', false, DATE_SUB(NOW(), INTERVAL 1 DAY)),

  -- Project 2: Kitchen Renovation (in progress, bid accepted)
  (2, 1, 44000, 7, 'Full kitchen renovation expertise. Will handle tiling, fitting, and plumbing liaison.', 'ACCEPTED', true,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (2, 2, 42000, 8, 'Competitive quote. Will source materials at wholesale.', 'REJECTED', false, DATE_SUB(NOW(), INTERVAL 3 DAY)),

  -- Project 3: Electrical Wiring (open)
  (3, 3, 14500, 3, 'Licensed electrician. Will handle full rewiring and safety certification.', 'PENDING', true, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- Update accepted bid reference on project 2
UPDATE projects SET accepted_bid_id = 5 WHERE id = 2;

-- ============================================================
-- CONVERSATIONS & MESSAGES
-- ============================================================
INSERT IGNORE INTO conversations (participant1_id, participant2_id, project_id, created_at) VALUES
  (6, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),  -- Priya ↔ Rajesh re: Apartment Painting
  (7, 1, 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),   -- Arjun ↔ Rajesh re: Villa Construction
  (6, 3, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR));  -- Priya ↔ Suresh re: Electrical

INSERT IGNORE INTO messages (conversation_id, sender_id, content, sent_at, is_read) VALUES
  (1, 6, 'Can you start the painting work tomorrow?',          DATE_SUB(NOW(), INTERVAL 2 HOUR),   false),
  (1, 1, 'Yes, I will be there at 9 AM with materials.',       DATE_SUB(NOW(), INTERVAL 90 MINUTE),true),
  (1, 6, 'Great! Please bring premium quality paint brushes.', DATE_SUB(NOW(), INTERVAL 30 MINUTE),false),

  (2, 7, 'Great work on the masonry! Payment released.',        DATE_SUB(NOW(), INTERVAL 2 DAY), true),
  (2, 1, 'Thank you sir! It was a pleasure working with you.', DATE_SUB(NOW(), INTERVAL 2 DAY), true),

  (3, 6, 'Please share your availability for next week.',      DATE_SUB(NOW(), INTERVAL 3 HOUR), false),
  (3, 3, 'I am available from Monday. What time suits you?',   DATE_SUB(NOW(), INTERVAL 2 HOUR), false);

-- ============================================================
-- REVIEWS (for worker 1 = Rajesh)
-- ============================================================
INSERT IGNORE INTO reviews (project_id, reviewer_id, reviewee_id, rating, comment, duration, project_name, client_name, created_at) VALUES
  (4, 7, 1, 5.0, 'Excellent work! Rajesh completed the masonry ahead of schedule with perfect quality.',
   '45 days', 'Residential Villa Construction', 'Arjun Mehta', DATE_SUB(NOW(), INTERVAL 30 DAY)),
  (5, 6, 1, 4.8, 'Very professional and skilled. The tiling work was done perfectly with clean finish.',
   '3 days',  'Bathroom Tiling',                'Priya Sharma', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ============================================================
-- EARNINGS (for owner 6 = Priya, last 7 days)
-- ============================================================
INSERT IGNORE INTO earnings (owner_id, project_id, amount, date) VALUES
  (6, 2, 8500,  DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
  (6, 2, 12000, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
  (6, 5, 15000, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
  (6, 5, 11000, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
  (6, 2, 18000, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
  (6, 2, 20000, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
  (6, 2, 9000,  CURDATE());

-- SCHEDULE EVENTS
INSERT IGNORE INTO schedule_events (worker_id, project_id, date, start_time, end_time) VALUES
  (1, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY),  '09:00:00', '17:00:00'),
  (3, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY),  '08:00:00', '16:00:00'),
  (4, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY),  '10:00:00', '18:00:00');
