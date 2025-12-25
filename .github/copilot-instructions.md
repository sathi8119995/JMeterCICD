# Copilot instructions — JMeter CI/CD project 🔧

Purpose
- Help AI coding agents be productive quickly: summarize project layout assumptions, key workflows (local runs, CI runs), important conventions, and examples the agent should follow when changing tests, adding features, or updating CI.

Quick rules for the agent ✅
- Always look for existing test plans under `tests/`, `tests/jmeter/`, or `jmeter/`. If none found, add a TODO and a short example `tests/jmeter/example_test.jmx` and a `README.md` in `tests/jmeter/` explaining how to run it.
- Use non-GUI JMeter runs for CI: `jmeter -n -t <test>.jmx -l results/<id>.jtl -e -o reports/<id>` (or the Docker/Maven equivalent shown below).
- When adding or changing load tests, add a short `run` snippet in the same folder showing the command used in CI and include expected artifact names (JTL and HTML report).

Project conventions and common file locations 🔎
- JMeter test plans: `tests/jmeter/*.jmx` (single-plan-per-file). Use descriptive names: e.g. `login_flow.jmx`, `api_endpoints.jmx`.
- Test data / CSV: `tests/jmeter/data/*.csv`.
- Test properties: `tests/jmeter/<test>.properties` or `tests/jmeter/env/*.properties`.
- Results & reports produced by CI: `results/` (JTL), `reports/` (HTML). CI jobs should upload these as artifacts.
- CI configurations: look for `Jenkinsfile`, `azure-pipelines.yml`, or `.github/workflows/*.yml`. Updating or adding tests usually requires updating the CI workflow to run the new plan.

How to run tests locally (examples) ▶️
- Docker (recommended if JMeter isn't installed locally):
  - POSIX shell:
    - `docker run --rm -v "$(pwd)":/tests -w /tests justb4/jmeter:5.4.1 jmeter -n -t tests/jmeter/my_test.jmx -l results/my_test.jtl -e -o reports/my_test`
  - PowerShell (Windows):
    - `docker run --rm -v "${PWD}:/tests" -w /tests justb4/jmeter:5.4.1 jmeter -n -t tests/jmeter/my_test.jmx -l results/my_test.jtl -e -o reports/my_test`
- Native JMeter CLI (if installed):
  - `jmeter -n -t tests/jmeter/my_test.jmx -l results/my_test.jtl -e -o reports/my_test`
- Maven projects: Search for `pom.xml` and `jmeter-maven-plugin` usage; run `mvn verify` or the plugin-specific goal if configured.

CI integration & expectations ⚙️
- CI runs MUST be non-GUI and deterministic (pass required properties/CSV files). Prefer running tests inside a container image that has a known JMeter version.
- CI jobs should:
  - Run only the intended test(s) or a smoke subset for PRs.
  - Save `results/*.jtl` and `reports/*` as pipeline artifacts for debugging.
  - Fail the job on test-plan script errors or if the JTL contains critical failures (assertions). Clarify thresholds in the workflow.

Debugging tips for agents 🐞
- If a PR introduces JMeter changes and CI fails: re-run the failing plan locally with same parameters and compare `jmeter.log` and `.jtl` output.
- Suggest adding an isolated, small-scale smoke plan (`tests/jmeter/smoke_*.jmx`) that runs quickly in PR checks.

What to change in pull requests ✍️
- When adding a new test plan:
  - Put the `.jmx` file in `tests/jmeter/` and include a minimal `.properties` (or note how to pass required parameters).
  - Add or update a script/snippet (`tests/jmeter/README.md`) showing the local and CI command.
  - Update CI workflow to include the plan (or add it to an existing matrix). Ensure artifacts are uploaded.
- When changing a CI workflow:
  - Preserve existing artifact upload and result collection behavior.
  - Add a short note in the workflow or a top-level `README.md` about how CI triggers load runs.

When you can't find required info (how the repo currently runs tests)
- Add a `TODO` in `.github/copilot-instructions.md` and a short issue/PR template comment asking: "Which CI provider (GitHub Actions / Jenkins / Azure Pipelines) should run JMeter tests? Where are canonical test plans located?"
- If the repo is missing test plans, prefer creating a minimal example `tests/jmeter/example_test.jmx`, a small CSV in `tests/jmeter/data/`, and a `tests/jmeter/README.md` with exact commands.

Agent etiquette / behavior
- Keep changes minimal and focused. When adding automation, include a short `README` entry and examples to make it reproducible by humans.
- Do not assume a specific CI provider—add TODOs where provider-specific changes are necessary.

---

If any of the above assumptions are incorrect for this repo, please tell me: 
- Where are test plans actually stored? (path)
- Which CI provider is used? (GitHub Actions / Jenkins / Azure / other)
- Are there any custom runner images or helper scripts to use?

I can revise the instructions to reference concrete files once you point me to them or push a small example test into `tests/jmeter/`.