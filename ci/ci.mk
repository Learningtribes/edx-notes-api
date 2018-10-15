.PHONY: ci_down ci_start ci_stop ci_test ci_up ci_clean

docker-compose.yml:
	envsubst < ci/docker-compose-template.yml > ci/docker-compose.yml

ci_up: docker-compose.yml ## Create containers used to run tests on Travis CI
	docker-compose -f ci/docker-compose.yml up -d

ci_start: docker-compose.yml ## Start containers stopped by `travis_stop`
	docker-compose -f ci/docker-compose.yml start

ci_test: ## Run tests on Docker containers, as on Travis CI
	docker exec edx_notes_api$(BUILD_TAG) /edx/app/edx_notes_api/edx_notes_api/ci/run_tests.sh

ci_clean:
	docker exec edx_notes_api$(BUILD_TAG) /edx/app/edx_notes_api/edx_notes_api/ci/clean_tests.sh

ci_stop: docker-compose.yml ## Stop running containers created by `travis_up` without removing them
	docker-compose -f ci/docker-compose.yml stop

ci_down: docker-compose.yml ## Stop and remove containers and other resources created by `travis_up`
	docker-compose -f ci/docker-compose.yml down
