-- Ortak sözleşme (docs/API_CONTRACT.md §2, §5.6): oturum iptali ve e-posta normalizasyonu.
ALTER TABLE users ADD COLUMN IF NOT EXISTS tokens_revoked_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS revoke_except_jti VARCHAR(64);

-- E-postalar küçük harf + kırpılmış saklanır (çakışma yaratacak kayıtlar olduğu gibi bırakılır).
UPDATE users u
SET email = LOWER(TRIM(u.email))
WHERE u.email <> LOWER(TRIM(u.email))
  AND NOT EXISTS (SELECT 1 FROM users o WHERE o.id <> u.id AND o.email = LOWER(TRIM(u.email)));
