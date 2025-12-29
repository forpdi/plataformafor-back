DELETE FROM password_reset_token;
DELETE FROM security_answer;
DELETE FROM reset_credentials;
DELETE FROM email_sent;
DELETE FROM proj_user where cpf <> '11111111111';
