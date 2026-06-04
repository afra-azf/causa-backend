/**
 * LLM Integration Layer
 *
 * <p>LangChain4J orchestration and multiple LLM provider support.
 *
 * <h2>Architecture Layer</h2>
 * Secondary Adapter (Outbound)
 *
 * <h2>Extensibility</h2>
 * Add new providers by implementing {@code core.ports.llm.LlmProvider} interface.
 * Factory automatically picks up via CDI.
 *
 * @since 0.0.1
 */
package com.causa.llm;
