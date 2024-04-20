package IPSwitch;

import java.util.List;

public class InterContinentalRouter extends Router {
    private final String continent;

    public InterContinentalRouter(int id, String name, Coordinates coordinates, String continent) {
        super(id, name, coordinates);
        this.continent = continent;
    }

    public String getContinent() {
        return continent;
    }

    @Override
    public String toString() {
        return "INTERCONTINENTAL ROUTER: " + getName() + String.format(", Coordinates: %s, Continent: %s", getCoordinates(), continent);
    }

    public static InterContinentalRouter getRouterByName(List<InterContinentalRouter> routers, String cityName) {
        for (InterContinentalRouter router : routers) {
            if (router.getName().equals(cityName)) {
                return router;
            }
        }
        return null;
    }
}
