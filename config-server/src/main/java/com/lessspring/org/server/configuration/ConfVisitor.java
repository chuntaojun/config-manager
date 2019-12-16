package com.lessspring.org.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/16 9:15 下午
 */
@Slf4j
public class ConfVisitor implements RouterFunctions.Visitor, RequestPredicates.Visitor {

	private static final Map<String, ConfVisitor> registerVisitor = new HashMap<>();

	private Set<RequestMethod> methods = new HashSet<>(3);
	private String path;
	private RouterFunction<?> routerFunction;

    public Set<RequestMethod> getMethods() {
        return methods;
    }

    public RouterFunction<?> getRouterFunction() {
        return routerFunction;
    }

    public String getPath() {
        return path;
    }

    public static ConfVisitor match(String path) {
		for (Map.Entry<String, ConfVisitor> entry : registerVisitor.entrySet()) {
			final String url = entry.getKey();
			if (url.contains(path) || path.contains(url)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public void startNested(RequestPredicate predicate) {
	}

	@Override
	public void endNested(RequestPredicate predicate) {
	}

	@Override
	public void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
	}

	@Override
	public void resources(Function<ServerRequest, Mono<Resource>> lookupFunction) {

	}

	@Override
	public void unknown(RouterFunction<?> routerFunction) {
	    this.routerFunction = routerFunction;
	}

	@Override
	public void method(Set<HttpMethod> methods) {
		this.methods = methods.stream()
				.map(httpMethod -> RequestMethod.valueOf(httpMethod.name()))
				.collect(Collectors.toSet());
	}

	@Override
	public void path(String pattern) {
		this.path = pattern;
		registerVisitor.put(pattern, this);
	}

	@Override
	public void pathExtension(String extension) {

	}

	@Override
	public void header(String name, String value) {
	}

	@Override
	public void queryParam(String name, String value) {
	}

	@Override
	public void startAnd() {

	}

	@Override
	public void and() {

	}

	@Override
	public void endAnd() {

	}

	@Override
	public void startOr() {

	}

	@Override
	public void or() {

	}

	@Override
	public void endOr() {

	}

	@Override
	public void startNegate() {

	}

	@Override
	public void endNegate() {

	}

	@Override
	public void unknown(RequestPredicate predicate) {

	}
}
