CREATE TABLE date_proposals
(
    id            UUID                        NOT NULL,
    proposer_id   UUID                        NOT NULL,
    invitee_id    UUID                        NOT NULL,
    proposed_time TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status        VARCHAR(32)                 NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_date_proposals PRIMARY KEY (id),
    CONSTRAINT fk_date_proposals_on_proposer FOREIGN KEY (proposer_id) REFERENCES members (id),
    CONSTRAINT fk_date_proposals_on_invitee FOREIGN KEY (invitee_id) REFERENCES members (id)
);

CREATE INDEX idx_date_proposals_proposer_id ON date_proposals (proposer_id);
CREATE INDEX idx_date_proposals_invitee_id ON date_proposals (invitee_id);
