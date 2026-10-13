INSERT INTO group_activities (student_group_id, actor_id, type, detail, occurred_at)
SELECT registration.student_group_id,
       registration.reviewed_by,
       CASE registration.status WHEN 'APPROVED' THEN 'APPROVED' ELSE 'REJECTED' END,
       CASE registration.status WHEN 'REJECTED' THEN registration.rejection_reason ELSE topic.title END,
       registration.reviewed_at
FROM topic_registrations registration
JOIN topics topic ON topic.id = registration.topic_id
WHERE registration.status IN ('APPROVED', 'REJECTED')
  AND registration.reviewed_by IS NOT NULL
  AND registration.reviewed_at IS NOT NULL;
