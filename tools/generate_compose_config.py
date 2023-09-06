#!/usr/bin/env python3
import logging
import sys
import pathlib
import yaml
import json
import jinja2

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)
repository_root_path = pathlib.Path(__file__).parent.parent
jinja_env = jinja2.Environment(
    loader=jinja2.FileSystemLoader(
        searchpath=[repository_root_path.joinpath("templates")],
        followlinks=True,
    ),
    autoescape=False,
)


def generate_impl_compose(specs_file):
    with specs_file.open("r") as fp:
        specs = json.load(fp)
    implementation_template = jinja_env.get_template("implementation.yml.tpl")
    with specs_file.parent.joinpath("compose.yml").open("w") as output_file:
        output_file.write(implementation_template.render(specs=specs))


def generate_combined_compose():
    files = {}
    for compose_file in sorted(
        repository_root_path.glob("images/*/compose.yml"),
        # Ensure that image names without a `-` are always sorted *after*
        # those that include a `-` (e.g. `openssh-7.x` comes before
        # `openssh`).
        key=lambda p: tuple(x or "~" for x in p.parent.stem.partition("-")),
    ):
        logger.debug("Parsed file: %s", compose_file)
        with compose_file.open("r") as fp:
            files[
                str(compose_file.relative_to(repository_root_path)).replace("\\", "/")
            ] = yaml.safe_load(fp)
    combined_template = jinja_env.get_template("combined.yml.tpl")
    with repository_root_path.joinpath("compose.yml").open("w") as output_file:
        output_file.write(
            combined_template.render(
                files=files,
                # Pass some builtin functions to make template development easier.
                enumerate=enumerate,
                sorted=sorted,
            ),
        )


def main():
    for specs_file in repository_root_path.glob("images/*/specs.json"):
        generate_impl_compose(specs_file)
    generate_combined_compose()
    return 0


if __name__ == "__main__":
    sys.exit(main())
