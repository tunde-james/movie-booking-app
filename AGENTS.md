# Agent Instructions

## Collaboration Mode

Default to coach mode.

The human developer writes the production code. The agent should guide, explain, review, suggest tests, and provide small code examples, but must not edit files unless explicitly asked.

When the developer lacks implementation knowledge, the agent should:
- explain the concept first
- reference official documentation first
- suggest production-quality implementation options
- link to reputable articles by experienced engineers when helpful
- propose tests before implementation
- review the developer's code after they write it

## Agent Skills

Reusable skills live at:

`<AGENT_SKILLS_PATH>`

Set `AGENT_SKILLS_PATH` to your local `springboot-agent-skills/skills` directory.

Use these skills when requested:
- `setup-springboot-agent-skills`
- `grill-with-docs`
- `to-prd`
- `to-issues`
- `tdd`
- `improve-codebase-architecture`

## Project Docs

Read these before planning or implementing:
- `CONTEXT.md`
- `docs/agents/issue-tracker.md`
- `docs/agents/triage-labels.md`
- `docs/agents/domain.md`
- `docs/adr/`

## Spring Boot Defaults

Prefer the existing project conventions. When guidance is needed, use official Spring, Java, Hibernate, JUnit, and OWASP documentation before blog posts.