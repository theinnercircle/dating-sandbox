CREATE TABLE date_proposal_history
(
    id          UUID                        NOT NULL,
    proposal_id UUID                        NOT NULL,
    member_id   UUID                        NOT NULL,
    action      VARCHAR(32)                 NOT NULL,
    occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_date_proposal_history PRIMARY KEY (id),
    CONSTRAINT fk_date_proposal_history_on_proposal FOREIGN KEY (proposal_id) REFERENCES date_proposals (id),
    CONSTRAINT fk_date_proposal_history_on_member FOREIGN KEY (member_id) REFERENCES members (id)
);

CREATE INDEX idx_date_proposal_history_proposal_id ON date_proposal_history (proposal_id);
