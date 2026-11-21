# Rule: Localization & Adventure Messaging

## Principles
1. **Adventure MiniMessage Baseline:** Use MiniMessage tags (`<green>`, `<bold>`, `<hover:...>`) for formatted server messages. Legacy section symbols (`§`) or ampersand color codes are strictly forbidden.
2. **Externalized Message Bundles:** Hardcoded user-visible strings in Java code are prohibited. All strings must reside in locale catalogs (`en-US.yml`, `pl-PL.yml`).
3. **Template Placeholders:** Dynamic variables in messages must use standard placeholders (e.g. `<tier>`, `<time_remaining>`) resolved via MiniMessage `Placeholder.parsed` or `TagResolver`.
4. **Relocation on Spigot:** On Spigot platforms, Adventure libraries must be shadowed and relocated to avoid classpath conflicts with other plugins.
