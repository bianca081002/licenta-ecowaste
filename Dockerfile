FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim

#Set environment variables
ENV WORKSPACE_ROOT=/opt/src \
    UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy


RUN mkdir -p $WORKSPACE_ROOT

#Install OS dependencies
RUN apt-get update -y \
    && apt-get install -y --no-install-recommends build-essential \
    gcc \
    libgnutls28-dev \
    libssl-dev \
    libcurl4-openssl-dev \
    libpq-dev \
    postgresql-client \
    && apt-get clean

WORKDIR $WORKSPACE_ROOT

#Install the project's dependencies using the lockfile and settings
RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync --frozen --no-install-project --no-dev


ADD . ${WORKSPACE_ROOT}
RUN --mount=type=cache,target=/root/.cache/uv \
    uv pip install --editable .

ENV PATH="${WORKSPACE_ROOT}/.venv/bin:$PATH"


#Reset the entrypoint, don't invoke uv
ENTRYPOINT []

CMD ["uvicorn", "src.main:fastapi_app", "--host", "0.0.0.0", "--port", "8000", "--reload"]
