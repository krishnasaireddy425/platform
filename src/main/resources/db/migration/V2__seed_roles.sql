INSERT INTO roles (id, org_id, name, description)
VALUES
  (gen_random_uuid(), NULL, 'OWNER', 'Full control over an org'),
  (gen_random_uuid(), NULL, 'ADMIN', 'Manage users & settings'),
  (gen_random_uuid(), NULL, 'USER',  'Standard member access');
