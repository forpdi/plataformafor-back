INSERT INTO proj_user
    (user_id, cpf, name, email, active, role, data_processing_agreement, created_date, updated_date)
VALUES
    ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff', '52015227261', 'User Test', 'user@teste.com', true, 'ROLE_ADMIN', false, now(), now());

INSERT INTO proj_user
    (user_id, cpf, name, email, active, role, data_processing_agreement, created_date, updated_date)
VALUES
    ('119e4b9a-1499-406a-8cc8-d33dcd02a3ff', '97501862060', 'User Test 2', 'user@teste.com', true, 'ROLE_ADMIN', false, now(), now());

INSERT INTO security_answer(
	user_id, question_id, answer, created_date, updated_date)
	VALUES ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff',
	    1,
	    'answer',
	    now(),
	    now());

INSERT INTO security_answer(
	user_id, question_id, answer, created_date, updated_date)
	VALUES ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff',
	    2,
	    'answer',
	    now(),
	    now());

INSERT INTO security_answer(
	user_id, question_id, answer, created_date, updated_date)
	VALUES ('389e4b9a-1499-406a-8cc8-d33dcd02a3ff',
	    3,
	    'answer',
	    now(),
	    now());
