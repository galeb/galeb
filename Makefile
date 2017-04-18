galeb-next: clean
	mvn package -DskipTests

test:
	mvn test

clean:
	mvn clean
	rm -f galeb-router-4.0.0-1.el6.noarch.rpm

dist: galeb-next
	type fpm > /dev/null 2>&1 && \
    cd router/target && \
    cp -a ../../wrapper . && \
    cp ../../wrapper.conf wrapper/conf/ && \
    cp -a ../../galeb-router.initscript wrapper/bin/ && \
    fpm -s dir \
        -t rpm \
        -n 'galeb-router' \
        -v 4.0.0 \
        --iteration 1.el6 \
        -a noarch \
        --rpm-os linux \
        --prefix /opt/galeb/router/lib \
        -m '<galeb@corp.globo.com>' \
        --vendor 'Globo.com' \
        --description 'Galeb Router service' \
        -p ../../galeb-router-4.0.0-1.el6.noarch.rpm *jar wrapper
