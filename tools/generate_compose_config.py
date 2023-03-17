#!/usr/bin/env python3
import argparse
import logging
import sys
import pathlib
import yaml
import jinja2


def main(argv=None):
    repository_root_path = pathlib.Path(__file__).parent.parent

    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger(__name__)

    parser = argparse.ArgumentParser()
    parser.add_argument(
        "file",
        type=pathlib.Path,
        nargs="*",
        default=sorted(
            repository_root_path.glob("images/*/compose.yml"),
            # Ensure that image names without a `-` are always sorted *after*
            # those that include a `-` (e.g. `openssh-7.x` comes before
            # `openssh`).
            key=lambda p: tuple(x or "~" for x in p.parent.stem.partition("-")),
        ),
    )
    parser.add_argument(
        "-t",
        "--template-file",
        type=str,
        default="compose.yml.tpl",
    )
    parser.add_argument(
        "-o", "--output-file", type=argparse.FileType("w"), default=sys.stdout
    )
    args = parser.parse_args(argv)

    files = {}
    for path in args.file:
        logger.debug("Parsed file: %s", path)
        with path.open("r") as fp:
            files[
                str(path.relative_to(repository_root_path)).replace("\\", "/")
            ] = yaml.safe_load(fp)

    env = jinja2.Environment(
        loader=jinja2.FileSystemLoader(
            searchpath=[repository_root_path],
            followlinks=True,
        ),
        autoescape=False,
    )
    logger.debug("Template file: %s", args.template_file)
    template = env.get_template(str(args.template_file))
    args.output_file.write(
        template.render(
            files=files,
            # Pass some builtin functions to make template development easier.
            enumerate=enumerate,
            sorted=sorted,
        ),
    )

    return 0


if __name__ == "__main__":
    sys.exit(main())
