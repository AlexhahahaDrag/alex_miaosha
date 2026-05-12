# Gift Schema Checklist

- Tables end with `_info_t`.
- Java business entities must not redeclare fields inherited from `BaseEntity`.
- SQL physical tables include the shared `BaseEntity` columns exactly once so MyBatis Plus can persist inherited fields.
- Tables include `org_id` and `user_id`.
- Time fields use `datetime` and map to `LocalDateTime`.
- Names use `snake_case`.
- `gift_record_info_t` has `direction`: `GIVE`, `RECEIVE`, `RETURN`.
- `gift_record_info_t` has `related_record_id` for `RETURN` rows.
- Indexes cover `org_id`, `user_id`, `event_id`, `giver_person_id`, `receiver_person_id`, `pay_time`, and `direction`.
- No `gift_return_record_info_t` table is created.
