INSERT INTO proj_user
    (user_id, cpf, name, email, active, role, data_processing_agreement, created_date, updated_date)
VALUES
    ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff', '52015227261', 'User Test', 'user@teste.com', true, 'ROLE_ADMIN', false, now(), now());

INSERT INTO proj_user
    (user_id, cpf, name, email, active, role, data_processing_agreement, created_date, updated_date)
VALUES
    ('389e4b9a-1499-406a-8cc8-d33dcd02a3fa', '95556661057', 'User Delete Test', 'user.delete@teste.com', true, 'ROLE_ADMIN', false, now(), now());

INSERT INTO security_answer(
	user_id, question_id, answer, created_date, updated_date)
	VALUES ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff',
	    1,
	    '9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08',
	    now(),
	    now());

INSERT INTO security_answer(
	user_id, question_id, answer, created_date, updated_date)
	VALUES ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff',
	    2,
	    '9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08',
	    now(),
	    now());

INSERT INTO security_answer(
	user_id, question_id, answer, created_date, updated_date)
	VALUES ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff',
	    3,
	    '9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08',
	    now(),
	    now());

INSERT INTO password_reset_token
    (token, used, user_id, created_date, updated_date)
VALUES
    ('389e4b9a-1499-406a-8cc8-d33dcd02a3f1', false, '389e4b9a-1499-406a-8cc8-d33dcd02a3ff',  now(), now());

INSERT INTO password_reset_token
    (token, used, user_id, created_date, updated_date)
VALUES
    ('389e4b9a-1499-406a-8cc8-d33dcd02a3f2', true, '389e4b9a-1499-406a-8cc8-d33dcd02a3ff',  now(), now());