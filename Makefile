TEST=.github/workflows/test.yaml
DOCS=.github/workflows/docs.yaml
GDS=.github/workflows/gds.yaml
PLATFORM=ubuntu-24.04=catthehacker/ubuntu:act-24.04
SECRET=GITHUB_TOKEN=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
ENV=ACTIONS_RUNTIME_TOKEN=12345
ARTIFACT=/tmp/artifacts

all: test
run_workflow_test:
	act --workflows $(TEST) --platform $(PLATFORM) --env $(ENV) --artifact-server-path $(ARTIFACT)
run_workflow_doc:
	act --workflows $(DOCS) --platform $(PLATFORM) --secret $(SECRET) --artifact-server-path $(ARTIFACT)
# Now working because of a bug in act
#run_workflow_gds:
#	act --workflows $(GDS) --platform $(PLATFORM) --env $(ENV) --artifact-server-path $(ARTIFACT)

generate_verilog:
	sbt "runMain tinygpu.Main"
test:
	make -C test
.PHONY: all generate_verilog run_workflow_test run_workflow_doc test
